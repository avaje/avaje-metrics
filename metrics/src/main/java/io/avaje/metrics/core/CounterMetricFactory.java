package io.avaje.metrics.core;

import io.avaje.metrics.CounterMetric;
import io.avaje.metrics.spi.SpiMetricBuilder;

final class CounterMetricFactory implements SpiMetricBuilder.Factory<CounterMetric> {

  @Override
  public CounterMetric createMetric(String name, int[] bucketRanges) {
    return new DCounterMetric(name);
  }

}
