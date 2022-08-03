package io.avaje.metrics.core.noop;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.ValueMetric;
import io.avaje.metrics.spi.SpiMetricBuilder;

public class NoopValueMetricFactory implements SpiMetricBuilder.Factory<ValueMetric> {

  @Override
  public ValueMetric createMetric(MetricName name, int[] bucketRanges) {
    return new NoopValueMetric(name);
  }

}
