package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import io.avaje.metrics.Timer;
import io.avaje.metrics.spi.SpiMetricBuilder;

final class BucketTimerFactory implements SpiMetricBuilder.Factory<Timer> {

  @Override
  public Timer createMetric(Metric.ID id, int[] bucketRanges) {
    int rangeBottom = 0;
    Timer[] buckets = new Timer[bucketRanges.length + 1];
    for (int i = 0; i < bucketRanges.length; i++) {
      int rangeTop = bucketRanges[i];
      buckets[i] = createTimedMetric(id, rangeBottom, rangeTop);
      // move the range bottom up to the last rangeTop
      rangeBottom = rangeTop;
    }
    buckets[bucketRanges.length] = createTimedMetric(id, rangeBottom, 0);
    return new DBucketTimer(id, bucketRanges, buckets);
  }

  private static Timer createTimedMetric(Metric.ID id, int rangeBottom, int rangeTop) {
    String suffix = (rangeTop == 0) ? String.valueOf(rangeBottom) : rangeBottom + "-" + rangeTop;
    return new DTimer(id, suffix);
  }

}
