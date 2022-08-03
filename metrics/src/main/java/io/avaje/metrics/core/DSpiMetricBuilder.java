package io.avaje.metrics.core;

import io.avaje.metrics.CounterMetric;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.ValueMetric;
import io.avaje.metrics.spi.MetricFactory;
import io.avaje.metrics.spi.SpiMetricBuilder;

final class DSpiMetricBuilder implements SpiMetricBuilder {

  @Override
  public MetricFactory<TimedMetric> timed() {
    return new TimedMetricFactory();
  }

  @Override
  public MetricFactory<TimedMetric> bucket() {
    return new BucketTimedMetricFactory();
  }

  @Override
  public MetricFactory<ValueMetric> value() {
    return new ValueMetricFactory();
  }

  @Override
  public MetricFactory<CounterMetric> counter() {
    return new CounterMetricFactory();
  }

}
