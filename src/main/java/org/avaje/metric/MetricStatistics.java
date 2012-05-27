package org.avaje.metric;


public interface MetricStatistics {
  
  public long getCount();

  public double getFifteenMinuteRate();


  public double getFiveMinuteRate();


  public double getMeanRate();


  public double getOneMinuteRate();

  /**
   * Returns the longest recorded duration.
   */
  public double getMax();

  /**
   * Returns the shortest recorded duration.
   */
  public double getMin();

  /**
   * Returns the arithmetic mean of all recorded durations.
   */
  public double getMean();

  /**
   * Returns the standard deviation of all recorded durations.
   */
  public double getStdDev();

  /**
   * Returns the sum of all recorded durations.
   */
  public double getSum();

  public MetricPercentiles getPercentiles();

}