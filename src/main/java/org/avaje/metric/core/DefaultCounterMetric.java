package org.avaje.metric.core;

import org.avaje.metric.CounterMetric;
import org.avaje.metric.MetricName;
import org.avaje.metric.statistics.CounterStatistics;
import org.avaje.metric.statistics.MetricStatisticsVisitor;


/**
 * Count events that occur.
 * <p>
 * For example, this is used to count the error events and warning events logged
 * via log4j or logback.
 * </p>
 */
final class DefaultCounterMetric implements CounterMetric {

  private final MetricName name;

  private final Counter counter;

  /**
   * Create the metric with a name and rateUnit.
   * <p>
   * The rateUnit should be chosen to 'scale' the statistics in a reasonable
   * manor - typically events per hour, minute or second.
   * </p>
   */
  DefaultCounterMetric(MetricName name) {
    this.name = name;
    this.counter = new Counter(name);
  }

  /**
   * Clear the collected statistics.
   */
  @Override
  public void clear() {
    counter.reset();
  }

  @Override
  public void collect(MetricStatisticsVisitor collector) {
    CounterStatistics stats = counter.collectStatistics();
    if (stats != null) {
      collector.visit(stats);
    }
  }

  @Override
  public long getCount() {
    return counter.getCount();
  }

  /**
   * Return the name of the metric.
   */
  @Override
  public MetricName getName() {
    return name;
  }

  /**
   * Mark that 1 event has occurred.
   */
  @Override
  public void markEvent() {
    counter.increment();
  }

  /**
   * Mark that numberOfEventsOccurred events have occurred.
   */
  @Override
  public void markEvents(long numberOfEventsOccurred) {
    counter.add(numberOfEventsOccurred);
  }

}
