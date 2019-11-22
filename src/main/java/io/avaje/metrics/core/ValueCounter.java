package io.avaje.metrics.core;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.statistics.TimedStatistics;

import java.util.concurrent.atomic.AtomicLong;
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

  private final MetricName name;

  private final boolean withBucket;

  private final String bucketRange;

  protected final LongAdder count = new LongAdder();

  private final LongAdder total = new LongAdder();

  protected final LongAccumulator max = new LongAccumulator(Math::max, Long.MIN_VALUE);

  private final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

  ValueCounter(MetricName name) {
    this.name = name;
    this.withBucket = false;
    this.bucketRange = noBuckets;
  }

  ValueCounter(MetricName name, String bucketRange) {
    this.name = name;
    this.withBucket = true;
    this.bucketRange = bucketRange;
  }

  MetricName name() {
    return name;
  }

  boolean isBucket() {
    return withBucket;
  }

  String getBucketRange() {
    return bucketRange;
  }

  /**
   * Add a value. Usually the value is Time or Bytes etc.
   */
  public void add(long value) {

    count.increment();
    total.add(value);
    max.accumulate(value);
  }

  public boolean isEmpty() {
    return count.sum() == 0;
  }

  TimedStatistics collectStatistics() {
    boolean empty = count.sum() == 0;
    if (empty) {
      startTime.set(System.currentTimeMillis());
      return null;
    } else {
      return getStatistics();
    }
  }

  /**
   * Return the current statistics resetting the internal values if reset is true.
   */
  private TimedStatistics getStatistics() {
    // Note these values are not guaranteed to be consistent wrt each other
    // but should be reasonably consistent (small time between count and total)
    final long maxVal = max.getThenReset();
    final long totalVal = total.sumThenReset();
    final long countVal = count.sumThenReset();
    final long startTimeVal = startTime.getAndSet(System.currentTimeMillis());
    return new DefaultValueStatistics(this, startTimeVal, countVal, totalVal, maxVal);
  }

  /**
   * Reset just the start time.
   */
  public void resetStartTime() {
    startTime.set(System.currentTimeMillis());
  }

  /**
   * Reset all the internal counters and start time.
   */
  public void reset() {
    startTime.set(System.currentTimeMillis());
    max.reset();
    count.reset();
    total.reset();
  }

  /**
   * Return the start time.
   */
  public long getStartTime() {
    return startTime.get();
  }

  /**
   * Return the count of values.
   */
  public long getCount() {
    return count.sum();
  }

  /**
   * Return the total of values.
   */
  public long getTotal() {
    return total.sum();
  }

  /**
   * Return the max value.
   */
  public long getMax() {
    return max.get();
  }

  public long getMean() {
    long count = getCount();
    long total = getTotal();
    return (count < 1) ? 0L : Math.round((double) (total / count));
  }

}
