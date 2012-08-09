package org.avaje.metric.stats;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.avaje.metric.MetricValueEvent;
import org.avaje.metric.Stats;

/**
 * Collects summary statistics in moving buffer. Typically used to hold
 * aggregation statistics on the last 5 minutes of activity.
 */
public class CollectMovingSummary {

  private final TimeUnit rateUnit;
  private long min = Long.MAX_VALUE;
  private long max = Long.MIN_VALUE;
  private long sum;
  private long count;

  private long resetStartTime = System.currentTimeMillis();

  private StatsSum lastSummary = new StatsSum();

  /**
   * Construct with the given rateUnit (used to determined event rate and load
   * rate).
   */
  public CollectMovingSummary(TimeUnit rateUnit) {
    this.rateUnit = rateUnit;
  }

  /**
   * Clears all recorded values.
   */
  protected void clearStats() {
    count = 0;
    sum = 0;
    max = Long.MIN_VALUE;
    min = Long.MAX_VALUE;
    resetStartTime = System.currentTimeMillis();
  }

  /**
   * Return the Summary potentially resetting the statistics.
   * <p>
   * The reset is typically used when collecting and reporting statistics
   * periodically (every 1 minute etc).
   * </p>
   */
  public Stats.Summary getSummary(boolean reset) {
    synchronized (this) {
      if (!reset) {
        return calcMerge();
      }
      StatsSum calc = calc();
      lastSummary = calc;
      clearStats();
      return calc;
    }
  }

  /**
   * Return the Summary without reseting the statistics.
   */
  public Stats.Summary getSummary() {
    synchronized (this) {
      return calcMerge();
    }
  }

  /**
   * This will be called relatively frequently (every few seconds) when the
   * events are 'flushed' to the statistics collection.
   */
  public long update(List<? extends MetricValueEvent> events) {

    synchronized (this) {

      long total = 0;

      for (int i = 0, max = events.size(); i < max; i++) {
        MetricValueEvent event = events.get(i);
        total += event.getValue();
        update(event.getValue());
      }

      return total;
    }
  }

  private StatsSum calc() {
    return new StatsSum(rateUnit, resetStartTime, count, sum, max(), min());
  }

  private StatsSum calcMerge() {
    if (lastSummary.getCount() == 0) {
      return new StatsSum(rateUnit, resetStartTime, count, sum, max(), min());
    }
    long newCount = count + lastSummary.getCount();
    double newSum = sum + lastSummary.getSum();
    double newMin = Math.min(min, lastSummary.getMin());
    double newMax = Math.max(max, lastSummary.getMax());
    long newStartTime = Math.min(resetStartTime, lastSummary.getStartTime());
    return new StatsSum(rateUnit, newStartTime, newCount, newSum, newMax, newMin);
  }

  private void update(long value) {

    count++;
    sum += value;
    setMax(value);
    setMin(value);
  }

  private double max() {
    if (count > 0) {
      return max;
    }
    return 0.0;
  }

  private double min() {
    if (count > 0) {
      return min;
    }
    return 0.0;
  }

  private void setMax(long potentialMax) {
    if (potentialMax > max) {
      max = potentialMax;
    }
  }

  private void setMin(long potentialMin) {
    if (potentialMin < min) {
      min = potentialMin;
    }
  }

}