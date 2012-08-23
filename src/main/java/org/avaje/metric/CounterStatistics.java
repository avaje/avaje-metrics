package org.avaje.metric;

/**
 * Counter statistics.
 */
public interface CounterStatistics extends Statistics {

  /**
   * Return the count of events.
   */
  public long getCount();

  /**
   * Return the duration in seconds.
   */
  public long getDuration();
  
  /**
   * Return the duration in milliseconds.
   */
  public long getDurationMillis();
  
}
