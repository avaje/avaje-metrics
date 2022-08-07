package io.avaje.metrics.core;

import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.spi.SpiMetricBuilder;

final class BucketTimedMetricFactory implements SpiMetricBuilder.Factory<TimedMetric> {

  @Override
  public TimedMetric createMetric(String name, int[] bucketRanges) {
    int rangeBottom = 0;
    TimedMetric[] buckets = new TimedMetric[bucketRanges.length + 1];
    for (int i = 0; i < bucketRanges.length; i++) {
      int rangeTop = bucketRanges[i];
      buckets[i] = createTimedMetric(name, rangeBottom, rangeTop);
      // move the range bottom up to the last rangeTop
      rangeBottom = rangeTop;
    }
    buckets[bucketRanges.length] = createTimedMetric(name, rangeBottom, 0);
    return new DBucketTimedMetric(name, bucketRanges, buckets);
  }

  private static TimedMetric createTimedMetric(String name, int rangeBottom, int rangeTop) {
    String suffix = (rangeTop == 0) ? String.valueOf(rangeBottom) : rangeBottom + "-" + rangeTop;
    return new DTimedMetric(name, suffix);
  }

}
