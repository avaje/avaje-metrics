package io.avaje.metrics.core.noop;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.ValueMetric;
import io.avaje.metrics.core.MetricFactory;

public class NoopValueMetricFactory implements MetricFactory<ValueMetric> {

  @Override
  public ValueMetric createMetric(MetricName name, int[] bucketRanges) {
    return new NoopValueMetric(name);
  }

}
