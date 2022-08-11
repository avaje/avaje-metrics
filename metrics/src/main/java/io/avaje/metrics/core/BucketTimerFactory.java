package io.avaje.metrics.core;

import io.avaje.metrics.Timer;
import io.avaje.metrics.spi.SpiMetricBuilder;

final class BucketTimerFactory implements SpiMetricBuilder.Factory<Timer> {

  @Override
  public Timer createMetric(String name, int[] bucketRanges) {
    int rangeBottom = 0;
    Timer[] buckets = new Timer[bucketRanges.length + 1];
    for (int i = 0; i < bucketRanges.length; i++) {
      int rangeTop = bucketRanges[i];
      buckets[i] = createTimedMetric(name, rangeBottom, rangeTop);
      // move the range bottom up to the last rangeTop
      rangeBottom = rangeTop;
    }
    buckets[bucketRanges.length] = createTimedMetric(name, rangeBottom, 0);
    return new DBucketTimer(name, bucketRanges, buckets);
  }

  private static Timer createTimedMetric(String name, int rangeBottom, int rangeTop) {
    String suffix = (rangeTop == 0) ? String.valueOf(rangeBottom) : rangeBottom + "-" + rangeTop;
    return new DTimer(name, suffix);
  }

}
