package org.avaje.metric.core.noop;

import org.avaje.metric.BucketTimedMetric;
import org.avaje.metric.MetricName;
import org.avaje.metric.core.MetricFactory;

public class NoopBucketTimedFactory implements MetricFactory<BucketTimedMetric> {

  @Override
  public BucketTimedMetric createMetric(MetricName name, int[] bucketRanges) {
        
    return new NoopBucketTimedMetric(name);
  }

}
