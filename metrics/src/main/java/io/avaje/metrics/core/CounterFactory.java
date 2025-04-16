package io.avaje.metrics.core;

import io.avaje.metrics.Counter;
import io.avaje.metrics.Metric;
import io.avaje.metrics.spi.SpiMetricBuilder;

final class CounterFactory implements SpiMetricBuilder.Factory<Counter> {

  @Override
  public Counter createMetric(Metric.ID id, int[] bucketRanges) {
    return new DCounter(id);
  }

}
