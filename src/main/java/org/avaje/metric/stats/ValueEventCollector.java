package org.avaje.metric.stats;

import static org.avaje.metric.NumFormat.fourdp;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.avaje.metric.Clock;
import org.avaje.metric.MetricStatistics;
import org.avaje.metric.MetricValueEvent;
import org.avaje.metric.Stats;
import org.avaje.metric.Stats.Summary;

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
    this.summaryStats = new CollectMovingSummary();
  }

  public void clear() {
    eventRate.clear();
    workRate.clear();
    summaryStats.clear();
  }

  public void update(List<? extends MetricValueEvent> events) {

    eventRate.updateAndTick(events.size());

    long totalWork = summaryStats.update(events);
    // total execution time or bytes or rows etc
    workRate.updateAndTick(totalWork);
  }

  public String toString() {
    Summary aggr = summaryStats.getAggregate();
    return "sinceSecs:" + aggr.getSinceSeconds()
        + " count:" + aggr.getCount() 
        + " min:" + fourdp(aggr.getMin()) 
        + " max:" + fourdp(aggr.getMax()) 
        + " sum:" + aggr.getSum()
        + " mean:" + fourdp(aggr.getMean()) 
        + " std:" + fourdp(aggr.getStdDev()) 
        + " 1minWork:" + fourdp(workRate.getOneMinuteRate()) 
        + " 1min:" + fourdp(eventRate.getOneMinuteRate()) 
        + " rateUnit:" + eventRate.getRateUnit();
  }

  @Override
  public Summary getSummary() {
    // return the last 5 minutes of aggregate
    return summaryStats.getAggregate();
  }

  public Summary getCurrentSummary() {
    return summaryStats.getCurrent();
  }
  
  public Stats.MovingAverages getEventRate() {
    return eventRate;
  }

  public Stats.MovingAverages getWorkRate() {
    return workRate;
  }

}
