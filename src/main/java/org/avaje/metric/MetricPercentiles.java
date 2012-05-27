package org.avaje.metric;

public interface MetricPercentiles {

  /**
   * Returns the median value in the distribution.
   */
  public double getMedian();

  /**
   * Returns the value at the 75th percentile in the distribution.
   */
  public double get75thPercentile();

  /**
   * Returns the value at the 95th percentile in the distribution.
   */
  public double get95thPercentile();

  /**
   * Returns the value at the 99th percentile in the distribution.
   */
  public double get99thPercentile();

  /**
   * Returns the value at the 99.9th percentile in the distribution.
   */
  public double get999thPercentile();

}