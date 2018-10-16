package org.avaje.metric.core;

import org.avaje.metric.statistics.MetricStatisticsVisitor;
import org.avaje.metric.statistics.TimedStatistics;


/**
 * Snapshot of the current statistics for a Counter or TimeCounter.
 */
class DefaultValueStatistics implements TimedStatistics {

  protected final ValueCounter owner;

  protected final long startTime;

  protected final long count;

  protected final long total;

  protected final long max;

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
    return owner.name().getSimpleName();
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
