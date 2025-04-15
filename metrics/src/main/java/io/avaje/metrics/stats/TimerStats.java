package io.avaje.metrics.stats;

import io.avaje.metrics.Metric;
import io.avaje.metrics.Timer;
import org.jspecify.annotations.Nullable;

/**
 * Snapshot of the current statistics for a Meter or Timer.
 */
public final class TimerStats implements Timer.Stats {

  final String name;
  final @Nullable String bucketRange;
  final long count;
  final long total;
  final long max;

  /**
   * Create with no bucketRange.
   */
  public TimerStats(String name, long count, long total, long max) {
    this(name, null, count, total, max);
  }

  /**
   * Create with all parameters including bucketRange.
   */
  public TimerStats(String name, @Nullable  String bucketRange, long count, long total, long max) {
    this.name = name;
    this.bucketRange = bucketRange;
    this.count = count;
    this.total = total;
    // collection is racy so sanitize the max value if it has not been set
    // this most likely would happen when count = 1 so max = mean
    this.max = max != Long.MIN_VALUE ? max : (count < 1 ? 0 : Math.round((float) total / count));
  }

  @Override
  public String toString() {
    return "count:" + count + " total:" + total + " max:" + max;
  }

  @Override
  public void visit(Metric.Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public @Nullable String bucketRange() {
    return bucketRange;
  }

  @Override
  public String name() {
    return name;
  }

  /**
   * Return the count of values collected.
   */
  @Override
  public long count() {
    return count;
  }

  /**
   * Return the total of all the values.
   */
  @Override
  public long total() {
    return total;
  }

  /**
   * Return the Max value collected.
   */
  @Override
  public long max() {
    return max;
  }

  /**
   * Return the mean value rounded up.
   */
  @Override
  public long mean() {
    return (count < 1) ? 0L : Math.round((double) (total / count));
  }

}
