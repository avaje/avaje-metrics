package io.avaje.metrics.spi;

import io.avaje.metrics.*;

public interface SpiMetricBuilder {

  Factory<TimedMetric> timed();
  Factory<TimedMetric> bucket();
  Factory<ValueMetric> value();
  Factory<CounterMetric> counter();

  /**
   * Factory for creating metrics.
   */
  interface Factory<T extends Metric> {

    /**
     * Create the metric.
     */
    T createMetric(MetricName name, int[] bucketRanges);
  }
}
