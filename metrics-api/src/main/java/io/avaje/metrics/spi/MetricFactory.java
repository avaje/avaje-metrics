package io.avaje.metrics.spi;

import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricName;

/**
 * Factory for creating metrics.
 */
public interface MetricFactory<T extends Metric> {

  /**
   * Create the metric.
   */
  T createMetric(MetricName name, int[] bucketRanges);

}
