package org.avaje.metric;

import java.util.concurrent.TimeUnit;

/**
 * A Timed Event typically used to time a SOAP operation or a SQL execution etc.
 * <p>
 * The events can end with either a successful execution or a error / fault
 * execution. The success and error statistics are collected and reported separately.
 * </p>
 */
public final class TimedMetricEvent implements MetricValueEvent {

  private final TimedMetric metric;
  private final long timeMillis;
  private final long startNanos;
  private long durationMillis;

  /**
   * Create a TimedMetricEvent.
   */
  protected TimedMetricEvent(TimedMetric metric) {
    this.metric = metric;
    this.timeMillis = metric.getTimeMillis();
    this.startNanos = metric.getTickNanos();
  }

  /**
   * Return the metric this event is for.
   */
  public TimedMetric getMetric() {
    return metric;
  }

  /**
   * Return the time the event occurred.
   */
  @Override
  public long getEventTime() {
    return timeMillis;
  }

  /**
   * Return the durationMillis of the timed event.
   */
  @Override
  public long getValue() {
    return durationMillis;
  }

  public String toString() {
    return metric.toString()+" durationMillis:" + getValue();
  }

  /**
   * This timed event ended with successful execution (e.g. Successful SOAP
   * Operation or SQL execution).
   */
  public void endWithSuccess() {
    this.durationMillis = TimeUnit.NANOSECONDS.toMillis(metric.getTickNanos() - startNanos);
    metric.endWithSuccess(this);
  }

  /**
   * This timed event ended with an error or fault execution (e.g. SOAP Fault or
   * SQL exception).
   */
  public void endWithError() {
    this.durationMillis = TimeUnit.NANOSECONDS.toMillis(metric.getTickNanos() - startNanos);
    metric.endWithError(this);
  }

}
