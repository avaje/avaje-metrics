package io.avaje.metrics.core.noop;

import io.avaje.metrics.Counter;
import io.avaje.metrics.Timer;
import io.avaje.metrics.Meter;
import io.avaje.metrics.spi.SpiMetricBuilder;

final class NoopMetricBuilder implements SpiMetricBuilder {

  @Override
  public Factory<Timer> timed() {
    return new NoopTimerFactory();
  }

  @Override
  public Factory<Timer> bucket() {
    return new NoopBucketTimerFactory();
  }

  @Override
  public Factory<Meter> value() {
    return new NoopMeterFactory();
  }

  @Override
  public Factory<Counter> counter() {
    return new NoopCounterFactory();
  }

}
