package org.avaje.metric.core;

import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;
import org.avaje.metric.ValueMetric;

public class ValueMetricFactory implements MetricFactory {

  @Override
  public Metric createMetric(MetricName name) {
    return new ValueMetric(name);
  }

}
