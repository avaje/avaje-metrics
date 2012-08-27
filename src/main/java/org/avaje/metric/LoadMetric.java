package org.avaje.metric;

import org.avaje.metric.stats.CollectLoadEvents;

/**
 * Measure 'aggregate' count and load events.
 * <p>
 * Compared with ValueMetric this takes an aggregation of count and load rather
 * than individual events. This means that min/max statistics are not collected
 * and instead LoadStatistics which just have the count and load (no min, max,
 * average etc).
 * </p>
 * <p>
 * This can be used to measure Garbage Collection rates.
 * </p>
 */
public final class LoadMetric implements Metric {

  private final MetricName name;

  private final CollectLoadEvents stats;

  /**
   * Create the metric.
   */
  public LoadMetric(MetricName name) {
    this.name = name;
    this.stats = new CollectLoadEvents();
  }

  @Override
  public void clearStatistics() {
    stats.reset();
  }

  public void updateStatistics() {
    // Do nothing, stats not updated in background but by the events themselves
  }

  public MetricName getName() {
    return name;
  }

  /**
   * Add an event with the total count and total load added.
   * 
   * @param eventCount
   *          the number of individual events added
   * @param eventLoad
   *          the total load for all the individual events added
   */
  public void addEvent(long eventCount, long eventLoad) {
    stats.update(eventCount, eventLoad);
  }

  /**
   * Return the load statistics.
   */
  public LoadStatistics getLoadStatistics(boolean reset) {
    return stats.getLoadStatistics(reset);
  }

  @Override
  public void visit(MetricVisitor visitor) {
    boolean empty = stats.isEmpty();
    if (!visitor.visitBegin(this, empty)) {
      if (empty) {
        // effectively reset the startTime
        stats.reset();
      }
    } else {
      visitor.visit(stats.getLoadStatistics(visitor.isResetStatistics()));
      visitor.visitEnd(this);
    }
  }

  public String toString() {
    return name + " " + stats.toString();
  }
}
