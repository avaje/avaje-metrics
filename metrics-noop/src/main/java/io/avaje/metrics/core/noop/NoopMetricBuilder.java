package io.avaje.metrics.core.noop;

import io.avaje.metrics.Counter;
import io.avaje.metrics.Timer;
import io.avaje.metrics.Meter;
import io.avaje.metrics.spi.SpiMetricBuilder;

final class NoopMetricBuilder implements SpiMetricBuilder {

  @Override
  public Factory<Timer> timed() {
    return new NoopTimedMetricFactory();
  }

  @Override
  public Factory<Timer> bucket() {
    return new NoopBucketTimedFactory();
  }

  @Override
  public Factory<Meter> value() {
    return new NoopValueMetricFactory();
  }

  @Override
  public Factory<Counter> counter() {
    return new NoopCounterMetricFactory();
  }

}
