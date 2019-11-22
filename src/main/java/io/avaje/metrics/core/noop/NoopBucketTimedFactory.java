package io.avaje.metrics.core.noop;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.core.MetricFactory;

public class NoopBucketTimedFactory implements MetricFactory<TimedMetric> {

  @Override
  public TimedMetric createMetric(MetricName name, int[] bucketRanges) {

    return new NoopBucketTimedMetric(name);
  }

}
