package io.avaje.metrics.core.noop;

import io.avaje.metrics.Timer;
import io.avaje.metrics.spi.SpiMetricBuilder;

public class NoopTimerFactory implements SpiMetricBuilder.Factory<Timer> {

  @Override
  public Timer createMetric(String name, int[] bucketRanges) {
    return new NoopTimer(name);
  }

}
