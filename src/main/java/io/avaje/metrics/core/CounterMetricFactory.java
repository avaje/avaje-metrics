package io.avaje.metrics.core;

import io.avaje.metrics.CounterMetric;
import io.avaje.metrics.MetricName;

class CounterMetricFactory implements MetricFactory<CounterMetric> {

  @Override
  public CounterMetric createMetric(MetricName name, int[] bucketRanges) {

    return new DefaultCounterMetric(name);
  }

}
