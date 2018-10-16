package org.avaje.metric.core;

import org.avaje.metric.MetricName;
import org.avaje.metric.statistics.CounterStatistics;
import org.avaje.metric.statistics.MetricStatisticsVisitor;

/**
 * Snapshot of the current statistics for a Counter or TimeCounter.
 */
class DefaultCounterStatistics implements CounterStatistics {

  protected final MetricName name;

  protected final long startTime;

  protected final long count;

  /**
   * Construct for Counter which doesn't collect time or high water mark.
   */
  DefaultCounterStatistics(MetricName name, long collectionStart, long count) {
    this.name = name;
    this.startTime = collectionStart;
    this.count = count;
  }

  @Override
  public void visit(MetricStatisticsVisitor visitor) {
    visitor.visit(this);
  }

  public String toString() {
    return "count:" + count;
  }

  @Override
  public String getName() {
    return name.getSimpleName();
  }

  /**
   * Return the time the counter started statistics collection.
   */
  public long getStartTime() {
    return startTime;
  }

  /**
   * Return the count of values collected.
   */
  public long getCount() {
    return count;
  }
}
