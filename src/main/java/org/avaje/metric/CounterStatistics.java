package org.avaje.metric;

/**
 * Counter statistics.
 */
public interface CounterStatistics {

  /**
   * Return the time these statistics started being collected.
   */
  public long getStartTime();
  
  /**
   * Return the duration in seconds.
   */
  public long getDuration();
  
  /**
   * Return the count of events.
   */
  public long getCount();
  
}
