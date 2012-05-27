package org.avaje.metric;

public interface MetricValueEvent {

  /**
   * Return the event time in milliseconds precision.
   */
  public long getEventTime();

  /**
   * Return the value of the event.
   */
  public long getValue();

}