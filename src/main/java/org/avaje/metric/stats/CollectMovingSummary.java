package org.avaje.metric.stats;

import java.util.List;

import org.avaje.metric.MetricValueEvent;
import org.avaje.metric.Stats;

/**
 * Collects summary statistics in moving buffer. Typically used to hold
 * aggregation statistics on the last 5 minutes of activity.
 */
public class CollectMovingSummary {

  private long min = Long.MAX_VALUE;
  private long max = Long.MIN_VALUE;
  private long sum;

  private long count;

  private final SummaryMovingBuffer movingSummary;

  private long lastAggregateTime = System.currentTimeMillis();

  private StatsSum longRunSummary = new StatsSum();

  /**
   * Create a 5 minute moving summary.
   */
  public CollectMovingSummary() {
    this(5);
  }

  /**
   * Create a moving summary with a specified buffer size in minutes.
   */
  public CollectMovingSummary(int bufferSizeInMinutes) {
    this.movingSummary = new SummaryMovingBuffer(bufferSizeInMinutes);
  }

  /**
   * Clears all recorded values.
   */
  public void clear() {
    count = 0;
    sum = 0;
    max = Long.MIN_VALUE;
    min = Long.MAX_VALUE;
    lastAggregateTime = System.currentTimeMillis();
  }

  /**
   * Reset the long run summary statistics.
   * <p>
   * This could be done hourly or daily depending on the need.
   * </p>
   */
  public void resetLongRunSummary() {
    this.longRunSummary = new StatsSum();
  }

  public Stats.Summary getLast() {
    return movingSummary.getLast();
  }

  public Stats.Summary getAggregate() {
    synchronized (this) {
      StatsSum movingAggregate = movingSummary.getMovingAggregate();
      // incorporate the recent data (in the last minute)
      return movingAggregate.merge( calc());
    }
  }

  public Stats.Summary getCurrent() {
    synchronized (this) {
      return calc();
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

      if (System.currentTimeMillis() - lastAggregateTime >= 60000) {
        // aggregate the statistics into the buffer every 1 minute
        aggregateStatistics();
      }

      return total;
    }
  }

  /**
   * Aggregate the statistics into the rolling buffer.
   */
  private void aggregateStatistics() {

    // get the summary for the last one minute of activity
    StatsSum lastOneMinSummary = calc();
    
    // maintain the long run (daily/hourly etc) aggregate statistics
    longRunSummary = longRunSummary.merge(lastOneMinSummary);

    // put the last 1 minutes summary into the buffer
    movingSummary.put(lastOneMinSummary);

    // reset the counters for the next minute
    count = 0;
    sum = 0;
    max = Long.MIN_VALUE;
    min = Long.MAX_VALUE;
    lastAggregateTime = System.currentTimeMillis();
  }

  private StatsSum calc() {
    return new StatsSum(lastAggregateTime, count, sum, max(), min());
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
