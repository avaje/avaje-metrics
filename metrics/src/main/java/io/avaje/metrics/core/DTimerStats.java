package io.avaje.metrics.core;

import io.avaje.metrics.Timer;
import io.avaje.metrics.MetricStatsVisitor;

/**
 * Snapshot of the current statistics for a Counter or TimeCounter.
 */
final class DTimerStats implements Timer.Stats {

  final ValueCounter owner;
  final long count;
  final long total;
  final long max;

  /**
   * Construct for TimeCounter.
   */
  DTimerStats(ValueCounter owner, long count, long total, long max) {
    this.owner = owner;
    this.count = count;
    this.total = total;
    // collection is racy so sanitize the max value if it has not been set
    // this most likely would happen when count = 1 so max = mean
    this.max = max != Long.MIN_VALUE ? max : (count < 1 ? 0 : Math.round(total / count));
  }

  @Override
  public String toString() {
    return "count:" + count + " total:" + total + " max:" + max;
  }

  @Override
  public void visit(MetricStatsVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public boolean isBucket() {
    return owner.isBucket();
  }

  @Override
  public String bucketRange() {
    return owner.bucketRange();
  }

  @Override
  public String name() {
    return owner.name();
  }

  @Override
  public String nameWithBucket() {
    return owner.nameWithBucket();
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
