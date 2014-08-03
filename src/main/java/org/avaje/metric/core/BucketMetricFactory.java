package org.avaje.metric.core;

import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;

/**
 * Factory for creating bucket based metrics.
 */
public interface BucketMetricFactory<T extends Metric> {

  /**
   * Create the metric.
   */
  public T createMetric(MetricName name, int... bucketRanges);

}