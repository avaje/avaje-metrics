package org.avaje.metric.core;

import org.avaje.metric.CounterMetric;
import org.avaje.metric.MetricName;

class CounterMetricFactory implements MetricFactory<CounterMetric> {

  @Override
  public CounterMetric createMetric(MetricName name, int[] bucketRanges) {

    return new DefaultCounterMetric(name);
  }

}
