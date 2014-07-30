package org.avaje.metric.core.noop;

import org.avaje.metric.MetricName;
import org.avaje.metric.ValueMetric;
import org.avaje.metric.core.MetricFactory;

public class NoopValueMetricFactory implements MetricFactory<ValueMetric> {

  @Override
  public ValueMetric createMetric(MetricName name) {
    return new NoopValueMetric(name);
  }

}
