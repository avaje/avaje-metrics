package org.avaje.metric;

import java.util.concurrent.TimeUnit;


public final class TimedMetricEvent implements MetricValueEvent {

  private final TimedMetric metric;
  private final long timeMillis;
  private final long startNanos;
  private long durationMillis;

  protected TimedMetricEvent(TimedMetric metric) {
    this.metric = metric;
    this.timeMillis = metric.getTimeMillis();
    this.startNanos = metric.getTickNanos();
  }
  

  @Override
  public long getEventTime() {
    return timeMillis;
  }
  

  @Override
  public long getValue() {
    return durationMillis;
  }
  
  public String toString() {
    return "durationMillis:"+getValue();
  }
  
  public void endWithSuccess() {
    this.durationMillis = TimeUnit.NANOSECONDS.toMillis(metric.getTickNanos() - startNanos);
    metric.endWithSuccess(this);
  }
  
  public void endWithError() {
    this.durationMillis = TimeUnit.NANOSECONDS.toMillis(metric.getTickNanos() - startNanos);
    metric.endWithError(this);
  }
  
}
