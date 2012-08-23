package org.avaje.metric.stats;

import static org.avaje.metric.NumFormat.onedp;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.avaje.metric.Clock;
import org.avaje.metric.MetricStatistics;
import org.avaje.metric.MetricValueEvent;
import org.avaje.metric.ValueStatistics;

/**
 * Collects statistics for ValueMetics.
 */
public class CollectValueEvents implements MetricStatistics {

  private final CollectMovingSummary summaryStats;

  public CollectValueEvents(TimeUnit rateUnit, Clock clock) {
    this.summaryStats = new CollectMovingSummary(rateUnit);
  }

  public void clear() {
    summaryStats.clearStats();
  }

  public void update(List<? extends MetricValueEvent> events) {

    summaryStats.update(events);
  }

  public String toString() {
    ValueStatistics aggr = summaryStats.getSummary();
    return "dur:" + aggr.getDuration() + " count:" + aggr.getCount() + " min:" + onedp(aggr.getMin()) + " avg:"
        + onedp(aggr.getMean()) + " max:" + onedp(aggr.getMax()) + " sum:" + aggr.getSum();
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

  public ValueStatistics getSummary() {
    return summaryStats.getSummary();
  }

  @Override
  public ValueStatistics getSummary(boolean reset) {
    return summaryStats.getSummaryStatistics(reset);
  }

}
