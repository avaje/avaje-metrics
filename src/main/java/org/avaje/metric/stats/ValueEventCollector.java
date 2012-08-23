package org.avaje.metric.stats;

import static org.avaje.metric.NumFormat.onedp;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.avaje.metric.Clock;
import org.avaje.metric.MetricStatistics;
import org.avaje.metric.MetricValueEvent;
import org.avaje.metric.MovingAverageStatistics;
import org.avaje.metric.SummaryStatistics;

/**
 * Collects statistics for ValueMetics.
 */
public class ValueEventCollector implements MetricStatistics {

  private final CollectMovingAverages eventRate;
  private final CollectMovingAverages workRate;
  private final CollectMovingSummary summaryStats;

  public ValueEventCollector(TimeUnit rateUnit, Clock clock) {
    this.eventRate = new CollectMovingAverages("eventRate", rateUnit, clock);
    this.workRate = new CollectMovingAverages("workRate", rateUnit, clock);
    this.summaryStats = new CollectMovingSummary(rateUnit);
  }

  public void clear() {
    eventRate.clear();
    workRate.clear();
    summaryStats.clearStats();
  }

  public void update(List<? extends MetricValueEvent> events) {

    eventRate.updateAndTick(events.size());

    long totalWork = summaryStats.update(events);
    // total execution time or bytes or rows etc
    workRate.updateAndTick(totalWork);
  }

  public String toString() {
    SummaryStatistics aggr = summaryStats.getSummary();
    return "sinceSecs:" + aggr.getDuration() + " count:" + aggr.getCount() + " min:"
        + onedp(aggr.getMin()) + " max:" + onedp(aggr.getMax()) + " sum:" + aggr.getSum()
        + " mean:" + onedp(aggr.getMean()) + " 1minWork:" + onedp(workRate.getOneMinuteRate())
        + " 1min:" + onedp(eventRate.getOneMinuteRate()) + " rateUnit:" + eventRate.getRateUnit();
  }

  /**
   * Return true if the summary statistics are empty (no events occurred).
   */
  public boolean isSummaryEmpty() {
    return summaryStats.isEmpty();
  }
  
  /**
   * Reset the summary statistics effectively moving the resetStartTime.
   */
  public void resetSummary() {
    summaryStats.reset();
  }
 
  public SummaryStatistics getSummary() {
    return summaryStats.getSummary();
  }

  @Override
  public SummaryStatistics getSummary(boolean reset) {
    return summaryStats.getSummary(reset);
  }

  public MovingAverageStatistics getEventRate() {
    return eventRate;
  }

  public MovingAverageStatistics getWorkRate() {
    return workRate;
  }

}
