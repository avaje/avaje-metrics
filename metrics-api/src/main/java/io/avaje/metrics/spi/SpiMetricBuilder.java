package io.avaje.metrics.spi;

import io.avaje.metrics.CounterMetric;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.ValueMetric;

public interface SpiMetricBuilder {

  MetricFactory<TimedMetric> timed();
  MetricFactory<TimedMetric> bucket();
  MetricFactory<ValueMetric> value();
  MetricFactory<CounterMetric> counter();

}
