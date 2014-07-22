package org.avaje.metric.core;

import org.avaje.metric.MetricName;
import org.avaje.metric.ValueMetric;

public class ValueMetricFactory implements MetricFactory<ValueMetric> {

  @Override
  public ValueMetric createMetric(MetricName name) {
    return new DefaultValueMetric(name);
  }

}
