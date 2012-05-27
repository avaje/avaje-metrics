package org.avaje.metric;

import java.util.concurrent.TimeUnit;

import org.avaje.metric.stats.RateMeter;


/**
 * Measure events that occur with a long value. This could be bytes or rows processed.
 */
public final class EventMetric implements Metric {

  private final MetricName name;
  
  private final Clock clock = Clock.defaultClock();
  
  private final RateMeter rateMeter;
  
  public EventMetric(MetricName name, TimeUnit rateUnit) {
  
    TimeUnit rateToUse = (rateUnit == null) ? TimeUnit.SECONDS : rateUnit;
    this.name = name;
    this.rateMeter = new RateMeter("events", rateToUse, clock);
  }

  public MetricRateStatistics getStatistics() {
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
