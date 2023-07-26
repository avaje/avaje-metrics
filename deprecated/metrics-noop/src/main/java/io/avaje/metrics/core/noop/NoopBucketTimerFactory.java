package io.avaje.metrics.core.noop;

import io.avaje.metrics.Timer;
import io.avaje.metrics.spi.SpiMetricBuilder;

public class NoopBucketTimerFactory implements SpiMetricBuilder.Factory<Timer> {

  @Override
  public Timer createMetric(String name, int[] bucketRanges) {
    return new NoopBucketTimer(name);
  }

}
