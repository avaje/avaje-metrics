package org.avaje.metric.stats;

import java.util.List;

import org.avaje.metric.MetricValueEvent;
import org.avaje.metric.ValueStatistics;

/**
 * Collects summary statistics in moving buffer. Typically used to hold
 * aggregation statistics on the last 5 minutes of activity.
 */
public class CollectMovingSummary {

  private long min = Long.MAX_VALUE;
  private long max = Long.MIN_VALUE;
  private long sum;
  private long count;

  private long resetStartTime = System.currentTimeMillis();

  private DSummaryStatistics lastSummary = new DSummaryStatistics();

  /**
   * Construct the CollectMovingSummary.
   */
  public CollectMovingSummary() {
  }

  /**
   * Clears all recorded values.
   */
  private void resetInternal() {
    count = 0;
    sum = 0;
    max = Long.MIN_VALUE;
    min = Long.MAX_VALUE;
    resetStartTime = System.currentTimeMillis();
  }

  public void reset() {
    synchronized (this) {
      resetInternal();
    }
  }

  
  public boolean isEmpty() {
    synchronized (this) {
      return count == 0;
    }
  }
  
  /**
   * Return the Summary potentially resetting the statistics.
   * <p>
   * The reset is typically used when collecting and reporting statistics
   * periodically (every 1 minute etc).
   * </p>
   */
  public ValueStatistics getValueStatistics(boolean reset) {
    synchronized (this) {
      if (!reset) {
        return calcMerge();
      }
      DSummaryStatistics calc = calc();
      lastSummary = calc;
      resetInternal();
      return calc;
    }
  }
  
  /**
   * Return the Summary without reseting the statistics.
   */
  public ValueStatistics getValueStatistics() {
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

  private DSummaryStatistics calc() {
    return new DSummaryStatistics(resetStartTime, count, sum, max(), min());
  }

  private DSummaryStatistics calcMerge() {
    if (lastSummary.getDuration() == 0) {
      return new DSummaryStatistics(resetStartTime, count, sum, max(), min());
    }
    long newCount = count + lastSummary.getCount();
    long newSum = sum + lastSummary.getSum();
    long newMin = Math.min(min, lastSummary.getMin());
    long newMax = Math.max(max, lastSummary.getMax());
    long newStartTime = Math.min(resetStartTime, lastSummary.getStartTime());
    return new DSummaryStatistics(newStartTime, newCount, newSum, newMax, newMin);
  }

  private void update(long value) {

    count++;
    sum += value;
    setMax(value);
    setMin(value);
  }

  private long max() {
    if (count > 0) {
      return max;
    }
    return 0;
  }

  private long min() {
    if (count > 0) {
      return min;
    }
    return 0;
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
