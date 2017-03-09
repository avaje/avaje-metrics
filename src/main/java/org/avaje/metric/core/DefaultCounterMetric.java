package org.avaje.metric.core;

import org.avaje.metric.CounterMetric;
import org.avaje.metric.CounterStatistics;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;
import org.avaje.metric.MetricVisitor;

import java.io.IOException;
import java.util.List;


/**
 * Count events that occur.
 * <p>
 * For example, this is used to count the error events and warning events logged
 * via log4j or logback.
 * </p>
 */
public final class DefaultCounterMetric implements Metric, CounterMetric {

  private final MetricName name;

  private final Counter counter = new Counter();

  private CounterStatistics collectedStatistics;

  /**
   * Create the metric with a name and rateUnit.
   * <p>
   * The rateUnit should be chosen to 'scale' the statistics in a reasonable
   * manor - typically events per hour, minute or second.
   * </p>
   */
  public DefaultCounterMetric(MetricName name) {
    this.name = name;
  }

  /**
   * Return the current statistics.
   */
  @Override
  public CounterStatistics getStatistics(boolean reset) {
    return counter.getStatistics(reset);
  }

  /**
   * Return the collected statistics. This is used by reporting objects.
   */
  @Override
  public CounterStatistics getCollectedStatistics() {
    return collectedStatistics;
  }

  /**
   * Clear the collected statistics.
   */
  @Override
  public void clearStatistics() {
    counter.reset();
  }

  /**
   * Collect the statistics returning true if there are non-zero statistics on this metric.
   */
  @Override
  public void collectStatistics(List<Metric> list) {
    this.collectedStatistics = counter.collectStatistics();
    if (collectedStatistics != null) {
      list.add(this);
    }
  }
  
  @Override
  public void visit(MetricVisitor visitor) throws IOException {
    visitor.visit(this);
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
