package org.avaje.metric;

/**
 * Summary of values.
 * <p>
 * Provides typical aggregate statistics min, average, max, count etc. Typically
 * these will be collected and reported every minute or so.
 * </p>
 */
public interface ValueStatistics {

  /**
   * Return the start time these aggregate statistics were started to be collected.
   */
  public long getStartTime();
  
  /**
   * Return the time in seconds over which this summary was collected.
   * <p>
   * Typically the statistics will be collected/reported every 60 seconds so
   * this would be 60 in that case.
   * </p>
   */
  public long getDuration();

  /**
   * Return the total count of events since the last reset.
   */
  public long getCount();

  /**
   * Return the total sum value.
   */
  public long getSum();

  /**
   * Return the maximum value.
   */
  public long getMax();

  /**
   * Return the minimum value.
   */
  public long getMin();

  /**
   * Return the mean value.
   */
  public double getMean();

}