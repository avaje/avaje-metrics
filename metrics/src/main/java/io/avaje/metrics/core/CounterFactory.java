package io.avaje.metrics.core;

import io.avaje.metrics.Counter;
import io.avaje.metrics.spi.SpiMetricBuilder;

final class CounterFactory implements SpiMetricBuilder.Factory<Counter> {

  @Override
  public Counter createMetric(String name, int[] bucketRanges) {
    return new DCounter(name);
  }

}
