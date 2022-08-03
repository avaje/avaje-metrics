package io.avaje.metrics.statistics;

/**
 * Statistics collected by ValueMetric or TimedMetric.
 */
public interface ValueStatistics extends MetricStatistics {

  /**
   * Return the count of values collected (since the last reset/collection).
   */
  long getCount();

  /**
   * Return the total of all the values (since the last reset/collection).
   */
  long getTotal();

  /**
   * Return the Max value collected (since the last reset/collection).
   */
  long getMax();

  /**
   * Return the mean value rounded up for the values collected since the last reset/collection.
   */
  long getMean();

}
