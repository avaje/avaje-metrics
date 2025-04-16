package io.avaje.metrics.core;

import io.avaje.metrics.Meter;
import io.avaje.metrics.Metric;
import io.avaje.metrics.spi.SpiMetricBuilder;

final class MeterFactory implements SpiMetricBuilder.Factory<Meter> {

  @Override
  public Meter createMetric(Metric.ID id, int[] bucketRanges) {
    return new DMeter(id);
  }

}
