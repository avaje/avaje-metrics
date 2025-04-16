package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import io.avaje.metrics.Timer;
import io.avaje.metrics.spi.SpiMetricBuilder;

final class TimerFactory implements SpiMetricBuilder.Factory<Timer> {

  @Override
  public Timer createMetric(Metric.ID id, int[] bucketRanges) {
    return new DTimer(id);
  }

}
