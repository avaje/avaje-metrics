package io.avaje.metrics.core;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.spi.SpiMetricBuilder;

final class TimedMetricFactory implements SpiMetricBuilder.Factory<TimedMetric> {

  @Override
  public TimedMetric createMetric(MetricName name, int[] bucketRanges) {
    return new DefaultTimedMetric(name);
  }

}
