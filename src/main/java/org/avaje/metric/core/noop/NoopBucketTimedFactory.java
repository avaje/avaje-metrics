package org.avaje.metric.core.noop;

import org.avaje.metric.MetricName;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.core.MetricFactory;

public class NoopBucketTimedFactory implements MetricFactory<TimedMetric> {

  @Override
  public TimedMetric createMetric(MetricName name, int[] bucketRanges) {

    return new NoopBucketTimedMetric(name);
  }

}
