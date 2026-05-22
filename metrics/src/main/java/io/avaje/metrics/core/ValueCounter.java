package io.avaje.metrics.core;

import io.avaje.metrics.CollectionMode;
import io.avaje.metrics.Meter;
import io.avaje.metrics.Metric;
import io.avaje.metrics.Timer;
import io.avaje.metrics.stats.MeterStats;
import io.avaje.metrics.stats.TimerStats;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * Used to collect long value statistics for meter and timer metrics.
 * <p>
 * It is intended for high concurrent updates to the statistics and relatively infrequent reads.
 * </p>
 */
final class ValueCounter extends BaseReportName {

  private final @Nullable String bucketRange;
  private final LongAdder count = new LongAdder();
  private final LongAdder total = new LongAdder();
  private final LongAccumulator max = new LongAccumulator(Math::max, 0);

  ValueCounter(Metric.ID id) {
    super(id);
    this.bucketRange = null;
  }

  ValueCounter(Metric.ID id, String bucketRange) {
    super(id);
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

  Meter.@Nullable Stats collect(Metric.Visitor collector, String unit) {
    var snapshot = collectSnapshot(collector);
    return snapshot == null ? null : new MeterStats(snapshot.reportId(), unit, snapshot.count(), snapshot.total(), snapshot.max());
  }

  Timer.@Nullable Stats collectTimed(Metric.Visitor collector) {
    var snapshot = collectSnapshot(collector);
    return snapshot == null ? null : new TimerStats(snapshot.reportId(), bucketRange, snapshot.count(), snapshot.total(), snapshot.max());
  }

  private @Nullable Snapshot collectSnapshot(Metric.Visitor collector) {
    final boolean cumulative = collector.collectionMode() == CollectionMode.CUMULATIVE;
    final long count = cumulative ? this.count.sum() : this.count.sumThenReset();
    if (count == 0) {
      return null;
    } else {
      final long totalVal = cumulative ? total.sum() : total.sumThenReset();
      final long maxVal = max.getThenReset();
      final Metric.ID reportId = reportId(collector);
      return new Snapshot(reportId, count, totalVal, maxVal);
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

  private static final class Snapshot {

    private final Metric.ID reportId;
    private final long count;
    private final long total;
    private final long max;

    private Snapshot(Metric.ID reportId, long count, long total, long max) {
      this.reportId = reportId;
      this.count = count;
      this.total = total;
      this.max = max;
    }

    private Metric.ID reportId() {
      return reportId;
    }

    private long count() {
      return count;
    }

    private long total() {
      return total;
    }

    private long max() {
      return max;
    }
  }

}
