package io.avaje.metrics.otel;

import io.avaje.applog.AppLog;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.lang.System.Logger.Level;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * Wraps a {@link SpanExporter} to track the latest in-flight export so that
 * a Lambda invocation can wait for an active background export to complete
 * before returning.
 *
 * <p>See {@link WaitingMetricExporter} for the pattern rationale.
 */
final class WaitingSpanExporter implements SpanExporter {

  private static final System.Logger log = AppLog.getLogger("io.avaje.metrics.otel");

  private final SpanExporter delegate;
  private volatile CompletableResultCode latestExport = CompletableResultCode.ofSuccess();

  WaitingSpanExporter(SpanExporter delegate) {
    this.delegate = requireNonNull(delegate, "delegate");
  }

  @Override
  public CompletableResultCode export(Collection<SpanData> spans) {
    long startNanos = System.nanoTime();
    int count = spans.size();
    log.log(Level.DEBUG, "OTLP span export starting count:{0}", count);
    var result = delegate.export(spans);
    latestExport = result;
    result.whenComplete(() -> {
      long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
      if (result.isSuccess()) {
        log.log(Level.DEBUG, "OTLP span export completed count:{0} elapsedMs:{1}", count, elapsedMs);
      } else {
        log.log(Level.WARNING, "OTLP span export failed count:" + count
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

  /**
   * Block until the latest export completes or the timeout elapses.
   * @return true if no export was active or the export completed in time
   */
  boolean waitIfRunning(long timeoutMillis) {
    var current = latestExport;
    if (current.isDone()) {
      return true;
    }
    log.log(Level.DEBUG, "Waiting up to {0}ms for in-flight OTLP span export", timeoutMillis);
    return current.join(timeoutMillis, TimeUnit.MILLISECONDS).isDone();
  }
}
