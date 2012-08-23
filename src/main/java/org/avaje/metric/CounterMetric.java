package org.avaje.metric;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

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

  private final CollectCounterEvents eventRate;

  private final AtomicLong counter = new AtomicLong(0);

  /**
   * Create the metric with a name and rateUnit.
   * <p>
   * The rateUnit should be chosen to 'scale' the statistics in a reasonable
   * manor - typically events per hour, minute or second.
   * </p>
   */
  public CounterMetric(MetricName name) {

    this.name = name;
    this.eventRate = new CollectCounterEvents();
  }

  @Override
  public TimeUnit getRateTimeUnit() {
    return null;
  }

  @Override
  public String getRateUnitAbbreviation() {
    return "";
  }

  public CounterStatistics getCounterStatistics() {
    return eventRate.getCounterStatistics(false);
  }

  /**
   * Clear the collected statistics.
   */
  @Override
  public void clearStatistics() {
    counter.set(0);
    eventRate.clear();
  }

  /**
   * Called periodically to update the collected statistics.
   */
  public void updateStatistics() {
    long eventCount = counter.getAndSet(0);
    eventRate.updateAndTick(eventCount);
  }

  @Override
  public void visit(MetricVisitor visitor) {
    boolean empty = eventRate.isEmpty();
    if (visitor.visitBegin(this, empty)) {
      visitor.visit(eventRate.getCounterStatistics(visitor.isResetStatistics()));
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
    counter.incrementAndGet();
  }

  /**
   * Mark that numberOfEventsOccurred events have occurred.
   */
  public void markEvents(long numberOfEventsOccurred) {
    counter.addAndGet(numberOfEventsOccurred);
  }

}
