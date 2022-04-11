package io.avaje.metrics.core;

import io.avaje.metrics.statistics.MetricStatisticsVisitor;
import io.avaje.metrics.statistics.TimedStatistics;


/**
 * Snapshot of the current statistics for a Counter or TimeCounter.
 */
final class DefaultValueStatistics implements TimedStatistics {

  final ValueCounter owner;
  final long startTime;
  final long count;
  final long total;
  final long max;

  /**
   * Construct for TimeCounter.
   */
  DefaultValueStatistics(ValueCounter owner, long collectionStart, long count, long total, long max) {
    this.owner = owner;
    this.startTime = collectionStart;
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
  public void visit(MetricStatisticsVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public boolean isBucket() {
    return owner.isBucket();
  }

  @Override
  public String getBucketRange() {
    return owner.getBucketRange();
  }

  @Override
  public String getName() {
    return owner.getName();
  }

  @Override
  public String getNameWithBucket() {
    return owner.getNameWithBucket();
  }

  /**
   * Return the time the counter started statistics collection.
   */
  @Override
  public long getStartTime() {
    return startTime;
  }

  /**
   * Return the count of values collected.
   */
  @Override
  public long getCount() {
    return count;
  }

  /**
   * Return the total of all the values.
   */
  @Override
  public long getTotal() {
    return total;
  }

  /**
   * Return the Max value collected.
   */
  @Override
  public long getMax() {
    return max;
  }

  /**
   * Return the mean value rounded up.
   */
  @Override
  public long getMean() {
    return (count < 1) ? 0L : Math.round((double) (total / count));
  }

}
