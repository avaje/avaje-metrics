package org.avaje.metric.core;

import org.avaje.metric.MetricName;
import org.avaje.metric.ValueMetric;

class ValueMetricFactory implements MetricFactory<ValueMetric> {

  @Override
  public ValueMetric createMetric(MetricName name, int[] bucketRanges) {
    return new DefaultValueMetric(name);
  }

}
