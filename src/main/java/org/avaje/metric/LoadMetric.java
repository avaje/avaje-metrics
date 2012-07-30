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

  /**
   * Create the metric with a name, rateUnit, event description and units for
   * the load.
   * <p>
   * The rateUnit should be chosen to 'scale' the statistics in a reasonable
   * manor - typically events per hour, minute or second.
   * </p>
   */
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
   * Return the moving averages for the rate of load occurring.
   */
  public Stats.MovingAverages getLoadMovingAverage() {
    return stats.getLoadMovingAverage();
  }

  /**
   * Return the moving averages for the rate that the events are occurring.
   */
  public Stats.MovingAverages getEventMovingAverage() {
    return stats.getEventMovingAverage();
  }
  
  @Override
  public void visit(MetricVisitor visitor) {
    visitor.visitBegin(this);
    visitor.visitEventRate(stats.getEventMovingAverage());
    visitor.visitLoadRate(stats.getLoadMovingAverage());
    visitor.visitEnd(this);
  }

  public String toString() {
    return name + " " + stats.toString();
  }
}
