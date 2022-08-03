package io.avaje.metrics.core;

import io.avaje.metrics.CounterMetric;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.ValueMetric;
import io.avaje.metrics.spi.SpiMetricBuilder;

final class DSpiMetricBuilder implements SpiMetricBuilder {

  @Override
  public Factory<TimedMetric> timed() {
    return new TimedMetricFactory();
  }

  @Override
  public Factory<TimedMetric> bucket() {
    return new BucketTimedMetricFactory();
  }

  @Override
  public Factory<ValueMetric> value() {
    return new ValueMetricFactory();
  }

  @Override
  public Factory<CounterMetric> counter() {
    return new CounterMetricFactory();
  }

}
