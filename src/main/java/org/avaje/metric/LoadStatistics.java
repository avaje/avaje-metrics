package org.avaje.metric;

/**
 * Counter statistics.
 */
public interface LoadStatistics {

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

  /**
   * Return the total load value.
   */
  public long getLoad();
  
}
