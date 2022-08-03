package io.avaje.metrics.core.noop;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.spi.SpiMetricBuilder;

public class NoopBucketTimedFactory implements SpiMetricBuilder.Factory<TimedMetric> {

  @Override
  public TimedMetric createMetric(MetricName name, int[] bucketRanges) {

    return new NoopBucketTimedMetric(name);
  }

}
