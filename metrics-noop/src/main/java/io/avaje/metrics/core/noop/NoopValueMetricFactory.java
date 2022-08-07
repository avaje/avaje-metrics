package io.avaje.metrics.core.noop;

import io.avaje.metrics.Meter;
import io.avaje.metrics.spi.SpiMetricBuilder;

public class NoopValueMetricFactory implements SpiMetricBuilder.Factory<Meter> {

  @Override
  public Meter createMetric(String name, int[] bucketRanges) {
    return new NoopValueMetric(name);
  }

}
