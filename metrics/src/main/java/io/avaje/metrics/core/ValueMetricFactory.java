package io.avaje.metrics.core;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.ValueMetric;
import io.avaje.metrics.spi.SpiMetricBuilder;

final class ValueMetricFactory implements SpiMetricBuilder.Factory<ValueMetric> {

  @Override
  public ValueMetric createMetric(MetricName name, int[] bucketRanges) {
    return new DValueMetric(name);
  }

}
