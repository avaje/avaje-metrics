package io.avaje.metrics.core.noop;

import io.avaje.metrics.CounterMetric;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.ValueMetric;
import io.avaje.metrics.spi.SpiMetricBuilder;

final class NoopMetricBuilder implements SpiMetricBuilder {

  @Override
  public Factory<TimedMetric> timed() {
    return new NoopTimedMetricFactory();
  }

  @Override
  public Factory<TimedMetric> bucket() {
    return new NoopBucketTimedFactory();
  }

  @Override
  public Factory<ValueMetric> value() {
    return new NoopValueMetricFactory();
  }

  @Override
  public Factory<CounterMetric> counter() {
    return new NoopCounterMetricFactory();
  }

}
