package org.avaje.metric;

import java.util.concurrent.TimeUnit;

import org.avaje.metric.stats.LoadCollector;

/**
 * Measure 'aggregate' count and load events.
 * <p>
 * Compared with ValueMetric this takes an aggregation of count and load rather
 * than individual events. This means that min/max statistics are not collected
 * and instead just the MovingAverages for the event rate and the load rate.
 * </p>
 * <p>
 * Also note that the statistics are collected in the foreground thread rather
 * than queued and calculated later.
 * </p>
 * <p>
 * This can be used to measure Garbage Collection.
 * </p>
 */
public final class LoadMetric implements Metric {

  private final MetricName name;

  private final Clock clock = Clock.defaultClock();

  private final LoadCollector stats;

  public LoadMetric(MetricName name, TimeUnit rateUnit, String eventDesc, String loadUnits) {

    TimeUnit rateToUse = (rateUnit == null) ? TimeUnit.SECONDS : rateUnit;
    this.name = name;
    this.stats = new LoadCollector(rateToUse, clock, eventDesc, loadUnits);
  }

  @Override
  public void clearStatistics() {
    stats.clear();
  }

  public void updateStatistics() {
    // Do nothing, stats not updated in background but by the events themselves
  }

  public MetricName getName() {
    return name;
  }

  public void addEvent(long eventCount, long eventLoad) {
    stats.update(eventCount, eventLoad);
  }

  public Stats.MovingAverages getLoadMovingAverage() {
    return stats.getLoadMovingAverage();
  }

  public Stats.MovingAverages getEventMovingAverage() {
    return stats.getEventMovingAverage();
  }

  public String toString() {
    return name + " " + stats.toString();
  }
}
