package io.avaje.metrics.core;

import io.avaje.metrics.Timer;
import io.avaje.metrics.spi.SpiSpan;
import org.jspecify.annotations.Nullable;

/**
 * A Timed Event typically used to time a SOAP operation or a SQL execution etc.
 * <p>
 * The events can end with either a successful execution or a error / fault
 * execution. The success and error statistics are collected and reported
 * separately.
 * </p>
 */
final class DTimerEvent implements Timer.Event {

  private final DTimer metric;
  private final @Nullable SpiSpan span;
  private final long startNanos;

  /**
   * Create a TimedMetricEvent.
   */
  DTimerEvent(DTimer metric, @Nullable SpiSpan span) {
    this.metric = metric;
    this.span = span;
    this.startNanos = DTimer.tickNanos();
  }

  @Override
  public String toString() {
    return metric + " durationMillis:" + duration();
  }

  /**
   * End specifying whether the event was successful or in error.
   */
  @Override
  public void end(boolean withSuccess) {
    end(withSuccess, null);
  }

  private void end(boolean withSuccess, @Nullable Throwable error) {
    metric.addEventDuration(withSuccess, duration());
    if (span != null) {
      if (withSuccess) {
        span.end();
      } else if (error != null) {
        span.endWithError(error);
      } else {
        span.endWithError();
      }
    }
  }

  /**
   * This timed event ended with successful execution (e.g. Successful SOAP
   * Operation or SQL execution).
   */
  @Override
  public void end() {
    end(true);
  }

  /**
   * This timed event ended with an error or fault execution (e.g. SOAP Fault or
   * SQL exception).
   */
  @Override
  public void endWithError() {
    end(false);
  }

  @Override
  public void endWithError(Throwable error) {
    end(false, error);
  }

  /**
   * Return the duration in nanos.
   */
  private long duration() {
    return DTimer.tickNanos() - startNanos;
  }

}
