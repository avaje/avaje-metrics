package io.avaje.metrics.core.noop;

import io.avaje.metrics.CounterMetric;
import io.avaje.metrics.MetricName;
import io.avaje.metrics.core.MetricFactory;

public class NoopCounterMetricFactory implements MetricFactory<CounterMetric> {

  @Override
  public CounterMetric createMetric(MetricName name, int[] bucketRanges) {
    return new NoopCounterMetric(name);
  }

}
