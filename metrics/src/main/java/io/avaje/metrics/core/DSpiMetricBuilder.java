package io.avaje.metrics.core;

import io.avaje.metrics.Counter;
import io.avaje.metrics.Timer;
import io.avaje.metrics.Meter;
import io.avaje.metrics.spi.SpiMetricBuilder;

final class DSpiMetricBuilder implements SpiMetricBuilder {

  @Override
  public Factory<Timer> timer() {
    return new TimerFactory();
  }

  @Override
  public Factory<Timer> bucket() {
    return new BucketTimerFactory();
  }

  @Override
  public Factory<Meter> meter() {
    return new MeterFactory();
  }

  @Override
  public Factory<Counter> counter() {
    return new CounterFactory();
  }

}
