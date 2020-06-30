package io.avaje.metrics.statistics;

/**
 * Statistics collected by TimedMetric.
 */
public interface TimedStatistics extends ValueStatistics {

  /**
   * Return true if this is bucket range based.
   */
  boolean isBucket();

  /**
   * Return the bucket range for these statistics.
   */
  String getBucketRange();

  /**
   * Return the metric name with bucket tag if necessary.
   * <p>
   * If the timed metric is a bucket it gets a "tag" appended
   * like <code>";bucket=.."</code>
   * </p>
   */
  String getNameWithBucket();
}
