package io.avaje.metrics.core;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.TimedMetric;

class BucketTimedMetricFactory implements MetricFactory<TimedMetric> {

  @Override
  public TimedMetric createMetric(MetricName name, int[] bucketRanges) {

    int rangeBottom = 0;

    TimedMetric[] buckets = new TimedMetric[bucketRanges.length+1];

    for (int i = 0; i < bucketRanges.length; i++) {
      int rangeTop = bucketRanges[i];
      buckets[i] = createTimedMetric(name, rangeBottom, rangeTop);
      // move the range bottom up to the last rangeTop
      rangeBottom = rangeTop;
    }
    buckets[bucketRanges.length] = createTimedMetric(name, rangeBottom, 0);

    return new DefaultBucketTimedMetric(name, bucketRanges, buckets);
  }

  private static TimedMetric createTimedMetric(MetricName name, int rangeBottom, int rangeTop) {
    String suffix = (rangeTop == 0) ? rangeBottom + "+" : rangeBottom + "-" + rangeTop;
    return new DefaultTimedMetric(name, suffix);
  }

}
