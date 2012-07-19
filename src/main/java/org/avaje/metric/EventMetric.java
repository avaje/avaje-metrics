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

  private final CollectMovingAverages rateMeter;

  public EventMetric(MetricName name, TimeUnit rateUnit) {

    TimeUnit rateToUse = (rateUnit == null) ? TimeUnit.SECONDS : rateUnit;
    this.name = name;
    this.rateMeter = new CollectMovingAverages("events", rateToUse, clock);
  }

  public Stats.MovingAverages getStatistics() {
    return rateMeter;
  }

  @Override
  public void clearStatistics() {
    rateMeter.clear();
  }

  public void updateStatistics() {
    rateMeter.tick();
  }

  public MetricName getName() {
    return name;
  }

  public void markEvent() {
    markEvents(1);
  }

  public void markEvents(long numberOfEventsOccurred) {
    rateMeter.update(numberOfEventsOccurred);
  }

}
