package io.avaje.metrics.spi;

import io.avaje.metrics.Counter;
import io.avaje.metrics.Meter;
import io.avaje.metrics.Metric;
import io.avaje.metrics.Timer;

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
    T createMetric(Metric.ID id, int[] bucketRanges);
  }
}
