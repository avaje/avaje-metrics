package io.avaje.metrics.core.noop;

import io.avaje.metrics.CounterMetric;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.ValueMetric;
import io.avaje.metrics.spi.MetricFactory;
import io.avaje.metrics.spi.SpiMetricBuilder;

final class NoopMetricBuilder implements SpiMetricBuilder {

  @Override
  public MetricFactory<TimedMetric> timed() {
    return new NoopTimedMetricFactory();
  }

  @Override
  public MetricFactory<TimedMetric> bucket() {
    return new NoopBucketTimedFactory();
  }

  @Override
  public MetricFactory<ValueMetric> value() {
    return new NoopValueMetricFactory();
  }

  @Override
  public MetricFactory<CounterMetric> counter() {
    return new NoopCounterMetricFactory();
  }

}
