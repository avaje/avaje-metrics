package org.avaje.metric;

import org.avaje.metric.report.MetricVisitor;


/**
 * Count events that occur.
 * <p>
 * For example, this is used to count the error events and warning events logged
 * via log4j or logback.
 * </p>
 */
public final class CounterMetric implements Metric {

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
  public CounterMetric(MetricName name) {
    this.name = name;
  }

  /**
   * Return the current statistics.
   */
  public CounterStatistics getStatistics(boolean reset) {
    return counter.getStatistics(reset);
  }

  /**
   * Return the collected statistics. This is used by reporting objects.
   */
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
  public boolean collectStatistics() {
    this.collectedStatistics = counter.collectStatistics();
    return collectedStatistics != null;
  }
  
  @Override
  public void visitCollectedStatistics(MetricVisitor visitor) {
    visitor.visit(this);
  }

  /**
   * Return the name of the metric.
   */
  public MetricName getName() {
    return name;
  }

  /**
   * Mark that 1 event has occurred.
   */
  public void markEvent() {
    counter.increment();
  }

  /**
   * Mark that numberOfEventsOccurred events have occurred.
   */
  public void markEvents(long numberOfEventsOccurred) {
    counter.add(numberOfEventsOccurred);
  }

}
