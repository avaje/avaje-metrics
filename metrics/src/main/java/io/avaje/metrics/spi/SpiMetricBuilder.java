package io.avaje.metrics.spi;

import io.avaje.metrics.*;

public interface SpiMetricBuilder {

  Factory<Timer> timer();
  Factory<Timer> bucket();
  Factory<Meter> meter();
  Factory<Counter> counter();

  /**
   * Factory for creating metrics.
   */
  interface Factory<T extends Metric> {

    /**
     * Create the metric.
     */
    T createMetric(String name, int[] bucketRanges);
  }
}
