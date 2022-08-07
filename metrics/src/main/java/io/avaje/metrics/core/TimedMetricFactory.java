package io.avaje.metrics.core;

import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.spi.SpiMetricBuilder;

final class TimedMetricFactory implements SpiMetricBuilder.Factory<TimedMetric> {

  @Override
  public TimedMetric createMetric(String name, int[] bucketRanges) {
    return new DTimedMetric(name);
  }

}
