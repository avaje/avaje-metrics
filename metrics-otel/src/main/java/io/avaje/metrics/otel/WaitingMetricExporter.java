package io.avaje.metrics.otel;

import io.avaje.applog.AppLog;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;

import java.lang.System.Logger.Level;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * Wraps a {@link MetricExporter} to track the latest in-flight export so that
 * a Lambda invocation can wait for an active background export to complete
 * before returning.
 *
 * <p>Mirrors the avaje-metrics StatsD {@code waitIfRunning()} pattern: most
 * invocations see no in-flight export and return immediately; occasionally
 * an invocation waits briefly while a scheduled background export finishes.
 * No additional export is triggered per invocation.
 */
final class WaitingMetricExporter implements MetricExporter {

  private static final System.Logger log = AppLog.getLogger("io.avaje.metrics.otel");

  private final MetricExporter delegate;
  private volatile CompletableResultCode latestExport = CompletableResultCode.ofSuccess();

  WaitingMetricExporter(MetricExporter delegate) {
    this.delegate = requireNonNull(delegate, "delegate");
  }

  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    long startNanos = System.nanoTime();
    int count = metrics.size();
    log.log(Level.DEBUG, "OTLP metric export starting count:{0}", count);
    var result = delegate.export(metrics);
    latestExport = result;
    result.whenComplete(() -> {
      long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
      if (result.isSuccess()) {
        log.log(Level.DEBUG, "OTLP metric export completed count:{0} elapsedMs:{1}", count, elapsedMs);
      } else {
        log.log(Level.WARNING, "OTLP metric export failed count:" + count
          + " elapsedMs:" + elapsedMs, result.getFailureThrowable());
      }
    });
    return result;
  }

  @Override
  public CompletableResultCode flush() {
    return delegate.flush();
  }

  @Override
  public CompletableResultCode shutdown() {
    return delegate.shutdown();
  }

  @Override
  public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return delegate.getAggregationTemporality(instrumentType);
  }

  @Override
  public Aggregation getDefaultAggregation(InstrumentType instrumentType) {
    return delegate.getDefaultAggregation(instrumentType);
  }

  @Override
  public MemoryMode getMemoryMode() {
    return delegate.getMemoryMode();
  }

  /**
   * Block until the latest export completes or the timeout elapses.
   * @return true if no export was active or the export completed in time
   */
  boolean waitIfRunning(long timeoutMillis) {
    var current = latestExport;
    if (current.isDone()) {
      return true;
    }
    log.log(Level.DEBUG, "Waiting up to {0}ms for in-flight OTLP metric export", timeoutMillis);
    return current.join(timeoutMillis, TimeUnit.MILLISECONDS).isDone();
  }
}
