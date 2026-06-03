package io.avaje.metrics.otel;

import io.avaje.applog.AppLog;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;

import java.lang.System.Logger.Level;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Waits for any in-flight OpenTelemetry metric or span export to complete and,
 * optionally, force-flushes telemetry that has gone stale.
 *
 * <p>Intended for use at the end of an AWS Lambda invocation (or similar
 * "freeze-on-exit" runtime) so that telemetry produced during the invocation
 * is delivered before the process is suspended. Mirrors the avaje-metrics
 * StatsD {@code waitIfRunning()} pattern: most invocations have zero overhead;
 * only an invocation that overlaps an active background export waits briefly.
 *
 * <p>If a {@code flushIfStale} duration is configured (see
 * {@link MetricsOpenTelemetry.WaiterBuilder#flushIfStale(Duration)}), the
 * waiter additionally triggers a {@code forceFlush()} on the SDK meter and
 * tracer providers when no successful export has completed within that
 * window. This makes the pattern adaptive: in busy runtimes the periodic
 * reader keeps {@code lastSuccess} fresh and forceFlush is a no-op; in low-
 * traffic runtimes (where the periodic reader is frozen between invocations)
 * forceFlush ships data on the invocation thread before the runtime freezes.
 *
 * <p>Obtain an instance via {@link MetricsOpenTelemetry.Builder#enableWaitIfRunning()}.
 */
public final class TelemetryWaiter {

  private static final System.Logger log = AppLog.getLogger("io.avaje.metrics.otel");

  private static final TelemetryWaiter NOOP = new TelemetryWaiter(null, null, null, 0L, Duration.ZERO);

  private final WaitingMetricExporter metricExporter;
  private final WaitingSpanExporter spanExporter;
  private final SdkMeterProvider meterProvider;
  private final SdkTracerProvider tracerProvider;
  private final long defaultTimeoutMillis;
  private final long flushIfStaleMillis;

  TelemetryWaiter(WaitingMetricExporter metricExporter,
                  WaitingSpanExporter spanExporter,
                  OpenTelemetrySdk sdk,
                  long defaultTimeoutMillis,
                  Duration flushIfStale) {
    this.metricExporter = metricExporter;
    this.spanExporter = spanExporter;
    this.meterProvider = sdk == null ? null : sdk.getSdkMeterProvider();
    this.tracerProvider = sdk == null ? null : sdk.getSdkTracerProvider();
    this.defaultTimeoutMillis = defaultTimeoutMillis;
    this.flushIfStaleMillis = flushIfStale == null ? 0L : Math.max(0L, flushIfStale.toMillis());
  }

  /**
   * A no-op waiter that performs no waiting and no flushing. Useful as a
   * default when waitIfRunning has not been enabled.
   */
  public static TelemetryWaiter noop() {
    return NOOP;
  }

  /**
   * Wait for any in-flight metric and span exports to complete using the
   * configured default timeout (per signal). Then, if {@code flushIfStale}
   * was configured and no successful export has completed within that
   * window, trigger a {@code forceFlush()} on the SDK providers.
   */
  public void waitIfRunning() {
    waitIfRunning(defaultTimeoutMillis, TimeUnit.MILLISECONDS);
  }

  /**
   * Wait for any in-flight metric and span exports to complete, applying the
   * given timeout to each signal independently. The same timeout is used for
   * any subsequent stale {@code forceFlush()}.
   */
  public void waitIfRunning(long timeout, TimeUnit unit) {
    long timeoutMillis = unit.toMillis(timeout);
    if (metricExporter != null && !metricExporter.waitIfRunning(timeoutMillis)) {
      log.log(Level.WARNING, "Timed out waiting " + timeoutMillis + "ms for OpenTelemetry metric export to complete");
    }
    if (spanExporter != null && !spanExporter.waitIfRunning(timeoutMillis)) {
      log.log(Level.WARNING, "Timed out waiting " + timeoutMillis + "ms for OpenTelemetry span export to complete");
    }
    flushIfStale(timeoutMillis);
  }

  /**
   * If a stale threshold is configured and the last successful export is
   * older than that threshold (or no export has succeeded yet), force-flush
   * the meter and tracer providers. Runs after {@link #waitIfRunning()} so
   * an in-flight tick that has just succeeded will have already updated
   * {@code lastSuccess}, making this a no-op.
   */
  private void flushIfStale(long timeoutMillis) {
    if (flushIfStaleMillis <= 0L) {
      return;
    }
    long now = System.currentTimeMillis();
    if (meterProvider != null && metricExporter != null
      && (now - metricExporter.lastSuccessAtMillis()) > flushIfStaleMillis) {
      long startNanos = System.nanoTime();
      log.log(Level.DEBUG, "OTLP metric forceFlush triggered (stale)");
      var result = meterProvider.forceFlush();
      result.join(timeoutMillis, TimeUnit.MILLISECONDS);
      long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
      if (result.isSuccess()) {
        log.log(Level.DEBUG, "OTLP metric forceFlush completed elapsedMs:{0}", elapsedMs);
      } else {
        log.log(Level.WARNING, "OTLP metric forceFlush did not complete elapsedMs:" + elapsedMs);
      }
    }
    if (tracerProvider != null && spanExporter != null
      && (now - spanExporter.lastSuccessAtMillis()) > flushIfStaleMillis) {
      long startNanos = System.nanoTime();
      log.log(Level.DEBUG, "OTLP span forceFlush triggered (stale)");
      var result = tracerProvider.forceFlush();
      result.join(timeoutMillis, TimeUnit.MILLISECONDS);
      long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
      if (result.isSuccess()) {
        log.log(Level.DEBUG, "OTLP span forceFlush completed elapsedMs:{0}", elapsedMs);
      } else {
        log.log(Level.WARNING, "OTLP span forceFlush did not complete elapsedMs:" + elapsedMs);
      }
    }
  }
}
