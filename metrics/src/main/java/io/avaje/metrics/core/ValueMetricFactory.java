package io.avaje.metrics.core;

import io.avaje.metrics.ValueMetric;
import io.avaje.metrics.spi.SpiMetricBuilder;

final class ValueMetricFactory implements SpiMetricBuilder.Factory<ValueMetric> {

  @Override
  public ValueMetric createMetric(String name, int[] bucketRanges) {
    return new DValueMetric(name);
  }

}
