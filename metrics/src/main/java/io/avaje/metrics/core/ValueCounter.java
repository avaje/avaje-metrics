package io.avaje.metrics.core;

import io.avaje.metrics.Timer;

import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * Used to collect timed execution statistics.
 * <p>
 * It is intended for high concurrent updates to the statistics and relatively infrequent reads.
 * </p>
 */
final class ValueCounter {

  private static final String noBuckets = "";

  private final String name;
  private final boolean withBucket;
  private final String nameWithBucket;
  private final String bucketRange;
  final LongAdder count = new LongAdder();
  private final LongAdder total = new LongAdder();
  final LongAccumulator max = new LongAccumulator(Math::max, Long.MIN_VALUE);

  ValueCounter(String name) {
    this.name = name;
    this.withBucket = false;
    this.bucketRange = noBuckets;
    this.nameWithBucket = name;
  }

  ValueCounter(String name, String bucketRange) {
    this.name = name;
    this.withBucket = true;
    this.bucketRange = bucketRange;
    this.nameWithBucket = this.name + ";bucket=" + bucketRange;
  }

  String name() {
    return name;
  }

  String nameWithBucket() {
    return nameWithBucket;
  }

  boolean isBucket() {
    return withBucket;
  }

  String bucketRange() {
    return bucketRange;
  }

  /**
   * Add a value. Usually the value is Time or Bytes etc.
   */
  void add(long value) {
    count.increment();
    total.add(value);
    max.accumulate(value);
  }

  Timer.Stats collect() {
    boolean empty = count.sum() == 0;
    if (empty) {
      return null;
    } else {
      return stats();
    }
  }

  /**
   * Return the current statistics resetting the internal values if reset is true.
   */
  private Timer.Stats stats() {
    // Note these values are not guaranteed to be consistent wrt each other
    // but should be reasonably consistent (small time between count and total)
    final long maxVal = max.getThenReset();
    final long totalVal = total.sumThenReset();
    final long countVal = count.sumThenReset();
    return new DTimerStats(this, countVal, totalVal, maxVal);
  }

  /**
   * Reset all the internal counters and start time.
   */
  void reset() {
    max.reset();
    count.reset();
    total.reset();
  }

  /**
   * Return the count of values.
   */
  long count() {
    return count.sum();
  }

  /**
   * Return the total of values.
   */
  long total() {
    return total.sum();
  }

  /**
   * Return the max value.
   */
  long max() {
    return max.get();
  }

  long mean() {
    long count = count();
    long total = total();
    return (count < 1) ? 0L : Math.round((double) (total / count));
  }

}
