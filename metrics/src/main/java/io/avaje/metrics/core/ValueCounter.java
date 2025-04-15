package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import io.avaje.metrics.Timer;
import io.avaje.metrics.stats.TimerStats;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * Used to collect timed execution statistics.
 * <p>
 * It is intended for high concurrent updates to the statistics and relatively infrequent reads.
 * </p>
 */
final class ValueCounter extends BaseReportName {

  private final @Nullable String bucketRange;
  private final LongAdder count = new LongAdder();
  private final LongAdder total = new LongAdder();
  private final LongAccumulator max = new LongAccumulator(Math::max, Long.MIN_VALUE);

  ValueCounter(String name) {
    super(name);
    this.bucketRange = null;
  }

  ValueCounter(String name, String bucketRange) {
    super(name);
    this.bucketRange = bucketRange;
  }

  @Override
  public String toString() {
    return "{" +
      "count=" + count +
      ", total=" + total +
      ", max=" + max +
      '}';
  }

  /**
   * Add a value. Usually the value is Time or Bytes etc.
   */
  void add(long value) {
    count.increment();
    total.add(value);
    max.accumulate(value);
  }

  Timer.@Nullable Stats collect(Metric.Visitor collector) {
    final long count = this.count.sumThenReset();
    if (count == 0) {
      return null;
    } else {
      final long maxVal = max.getThenReset();
      final long totalVal = total.sumThenReset();
      final String name = reportName != null ? reportName : reportName(collector);
      return new TimerStats(name, bucketRange, count, totalVal, maxVal);
    }
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
