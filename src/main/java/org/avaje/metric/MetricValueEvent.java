package org.avaje.metric;

/**
 * A value event.
 * <p>
 * The value typically represents execution time or bytes or rows etc.
 * </p>
 */
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