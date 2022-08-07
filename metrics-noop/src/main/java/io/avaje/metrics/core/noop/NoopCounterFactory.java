package io.avaje.metrics.core.noop;

import io.avaje.metrics.Counter;
import io.avaje.metrics.spi.SpiMetricBuilder;

public class NoopCounterFactory implements SpiMetricBuilder.Factory<Counter> {

  @Override
  public Counter createMetric(String name, int[] bucketRanges) {
    return new NoopCounter(name);
  }

}
