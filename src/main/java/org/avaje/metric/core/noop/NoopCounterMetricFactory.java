package org.avaje.metric.core.noop;

import org.avaje.metric.CounterMetric;
import org.avaje.metric.MetricName;
import org.avaje.metric.core.MetricFactory;

public class NoopCounterMetricFactory implements MetricFactory<CounterMetric> {

  @Override
  public CounterMetric createMetric(MetricName name, int[] bucketRanges) {
    return new NoopCounterMetric(name);
  }

}
