package io.avaje.metrics.core;

import io.avaje.metrics.TimedEvent;


/**
 * A Timed Event typically used to time a SOAP operation or a SQL execution etc.
 * <p>
 * The events can end with either a successful execution or a error / fault
 * execution. The success and error statistics are collected and reported
 * separately.
 * </p>
 */
final class DefaultTimedMetricEvent implements TimedEvent {

  private final DefaultTimedMetric metric;

  private final long startNanos;

  /**
   * Create a TimedMetricEvent.
   */
  DefaultTimedMetricEvent(DefaultTimedMetric metric) {
    this.metric = metric;
    this.startNanos = DefaultTimedMetric.getTickNanos();
  }

  public String toString() {
    return metric.toString() + " durationMillis:" + getDuration();
  }

  /**
   * End specifying whether the event was successful or in error.
   */
  @Override
  public void end(boolean withSuccess) {
    metric.addEventDuration(withSuccess, getDuration());
  }

  /**
   * This timed event ended with successful execution (e.g. Successful SOAP
   * Operation or SQL execution).
   */
  @Override
  public void endWithSuccess() {
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


  /**
   * Return the duration in nanos.
   */
  private long getDuration() {
    return DefaultTimedMetric.getTickNanos() - startNanos;
  }

}
