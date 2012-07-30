package org.avaje.metric;

import java.util.concurrent.TimeUnit;

import org.avaje.metric.stats.CollectMovingAverages;

/**
 * Measure events that occur. This just measures the rate that the events occur
 * so there is no account for the 'load' of each event.
 * <p>
 * This could be used to measure things like error events or warning events.
 * </p>
 */
public final class EventMetric implements Metric {

  private final MetricName name;

  private final Clock clock = Clock.defaultClock();

  private final CollectMovingAverages eventRate;

  /**
   * Create the metric with a name and rateUnit.
   * <p>
   * The rateUnit should be chosen to 'scale' the statistics in a reasonable
   * manor - typically events per hour, minute or second.
   * </p>
   */
  public EventMetric(MetricName name, TimeUnit rateUnit) {

    TimeUnit rateToUse = (rateUnit == null) ? TimeUnit.SECONDS : rateUnit;
    this.name = name;
    this.eventRate = new CollectMovingAverages("events", rateToUse, clock);
  }

  /**
   * Return the moving average statistics for the rate of events occurring.
   */
  public Stats.MovingAverages getEventMovingAverage() {
    return eventRate;
  }

  /**
   * Clear the collected statistics.
   */
  @Override
  public void clearStatistics() {
    eventRate.clear();
  }

  /**
   * Called periodically to update the collected statistics.
   */
  public void updateStatistics() {
    eventRate.tick();
  }
  
  @Override
  public void visit(MetricVisitor visitor) {
    visitor.visitBegin(this);
    visitor.visitEventRate(eventRate);
    visitor.visitEnd(this);
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
    markEvents(1);
  }

  /**
   * Mark that numberOfEventsOccurred events have occurred.
   */
  public void markEvents(long numberOfEventsOccurred) {
    eventRate.update(numberOfEventsOccurred);
  }

}
