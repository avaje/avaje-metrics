package io.avaje.metrics.core.noop;

import io.avaje.metrics.ValueMetric;
import io.avaje.metrics.spi.SpiMetricBuilder;

public class NoopValueMetricFactory implements SpiMetricBuilder.Factory<ValueMetric> {

  @Override
  public ValueMetric createMetric(String name, int[] bucketRanges) {
    return new NoopValueMetric(name);
  }

}
