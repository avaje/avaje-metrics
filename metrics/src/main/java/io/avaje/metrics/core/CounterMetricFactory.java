package io.avaje.metrics.core;

import io.avaje.metrics.CounterMetric;
import io.avaje.metrics.MetricName;
import io.avaje.metrics.spi.SpiMetricBuilder;

final class CounterMetricFactory implements SpiMetricBuilder.Factory<CounterMetric> {

  @Override
  public CounterMetric createMetric(MetricName name, int[] bucketRanges) {
    return new DCounterMetric(name);
  }

}
