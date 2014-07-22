package org.avaje.metric.core;

import org.avaje.metric.CounterMetric;
import org.avaje.metric.MetricName;

public class CounterMetricFactory implements MetricFactory<CounterMetric> {

  @Override
  public CounterMetric createMetric(MetricName name) {

    return new DefaultCounterMetric(name);
  }

}
