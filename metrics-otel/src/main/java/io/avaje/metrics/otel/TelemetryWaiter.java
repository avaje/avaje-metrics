package io.avaje.metrics.otel;

import io.avaje.applog.AppLog;

import java.lang.System.Logger.Level;
import java.util.concurrent.TimeUnit;

/**
 * Waits for any in-flight OpenTelemetry metric or span export to complete.
 *
 * <p>Intended for use at the end of an AWS Lambda invocation (or similar
 * "freeze-on-exit" runtime) so that telemetry produced during the invocation
 * is delivered before the process is suspended. Mirrors the avaje-metrics
 * StatsD {@code waitIfRunning()} pattern: most invocations have zero overhead;
 * only an invocation that overlaps an active background export waits briefly.
 *
 * <p>This does <strong>not</strong> trigger an export. Background reporting
 * via the {@link io.opentelemetry.sdk.metrics.export.PeriodicMetricReader} and
 * {@link io.opentelemetry.sdk.trace.export.BatchSpanProcessor} continues on its
 * normal schedule.
 *
 * <p>Obtain an instance via {@link MetricsOpenTelemetry.Builder#enableWaitIfRunning()}.
 */
public final class TelemetryWaiter {

  private static final System.Logger log = AppLog.getLogger("io.avaje.metrics.otel");

  private static final TelemetryWaiter NOOP = new TelemetryWaiter(null, null, 0L);

  private final WaitingMetricExporter metricExporter;
  private final WaitingSpanExporter spanExporter;
  private final long defaultTimeoutMillis;

  TelemetryWaiter(WaitingMetricExporter metricExporter,
                  WaitingSpanExporter spanExporter,
                  long defaultTimeoutMillis) {
    this.metricExporter = metricExporter;
    this.spanExporter = spanExporter;
    this.defaultTimeoutMillis = defaultTimeoutMillis;
  }

  /**
   * A no-op waiter that performs no waiting. Useful as a default when
   * waitIfRunning has not been enabled.
   */
  public static TelemetryWaiter noop() {
    return NOOP;
  }

  /**
   * Wait for any in-flight metric and span exports to complete using the
   * configured default timeout (per signal).
   */
  public void waitIfRunning() {
    waitIfRunning(defaultTimeoutMillis, TimeUnit.MILLISECONDS);
  }

  /**
   * Wait for any in-flight metric and span exports to complete, applying the
   * given timeout to each signal independently.
   */
  public void waitIfRunning(long timeout, TimeUnit unit) {
    long timeoutMillis = unit.toMillis(timeout);
    if (metricExporter != null && !metricExporter.waitIfRunning(timeoutMillis)) {
      log.log(Level.WARNING, "Timed out waiting " + timeoutMillis + "ms for OpenTelemetry metric export to complete");
    }
    if (spanExporter != null && !spanExporter.waitIfRunning(timeoutMillis)) {
      log.log(Level.WARNING, "Timed out waiting " + timeoutMillis + "ms for OpenTelemetry span export to complete");
    }
  }
}
