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
final class DTimedMetricEvent implements TimedEvent {

  private final DTimedMetric metric;
  private final long startNanos;

  /**
   * Create a TimedMetricEvent.
   */
  DTimedMetricEvent(DTimedMetric metric) {
    this.metric = metric;
    this.startNanos = DTimedMetric.tickNanos();
  }

  @Override
  public String toString() {
    return metric.toString() + " durationMillis:" + duration();
  }

  /**
   * End specifying whether the event was successful or in error.
   */
  @Override
  public void end(boolean withSuccess) {
    metric.addEventDuration(withSuccess, duration());
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

  /**
   * Return the duration in nanos.
   */
  private long duration() {
    return DTimedMetric.tickNanos() - startNanos;
  }

}
