package org.avaje.metric;

import org.avaje.metric.stats.CollectCounterEvents;

/**
 * Count events that occur.
 * <p>
 * For example, this is used to count the error events and warning events logged
 * via log4j or logback.
 * </p>
 */
public final class CounterMetric implements Metric {

  private final MetricName name;

  private final CollectCounterEvents counter;

  /**
   * Create the metric with a name and rateUnit.
   * <p>
   * The rateUnit should be chosen to 'scale' the statistics in a reasonable
   * manor - typically events per hour, minute or second.
   * </p>
   */
  public CounterMetric(MetricName name) {
    this.name = name;
    this.counter = new CollectCounterEvents();
  }

  public CounterStatistics getCounterStatistics() {
    return counter.getCounterStatistics(false);
  }

  /**
   * Clear the collected statistics.
   */
  @Override
  public void clearStatistics() {
    counter.reset();
  }

  /**
   * Called periodically to update the collected statistics.
   */
  public void updateStatistics() {
    // Nothing to do
  }

  @Override
  public void visit(MetricVisitor visitor) {
    boolean emptyMetric = counter.isEmpty();
    if (!visitor.visitBegin(this, emptyMetric)) {
      if (emptyMetric) {
        // effectively reset the start time
        counter.reset();
      }      
    } else {
      visitor.visit(counter.getCounterStatistics(visitor.isResetStatistics()));
      visitor.visitEnd(this);
    }
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
