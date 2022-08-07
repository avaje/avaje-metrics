package io.avaje.metrics.core.noop;

import io.avaje.metrics.Timer;
import io.avaje.metrics.spi.SpiMetricBuilder;

public class NoopTimedMetricFactory implements SpiMetricBuilder.Factory<Timer> {

  @Override
  public Timer createMetric(String name, int[] bucketRanges) {
    return new NoopTimedMetric(name);
  }

}
