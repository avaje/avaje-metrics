package org.avaje.metric.stats;

import org.avaje.metric.ValueStatistics;

public class DSummaryStatistics implements ValueStatistics {

  private final long startTime;
  private final long duration;
  private final long count;
  private final long sum;
  private final long max;
  private final long min;
  private final double mean;

  public DSummaryStatistics() {
    this.startTime = System.currentTimeMillis();
    this.count = 0;
    this.sum = 0;
    this.max = Long.MIN_VALUE;
    this.min = Long.MAX_VALUE;
    this.mean = 0;
    this.duration = 0;
  }

  public DSummaryStatistics(long startTime, long count, long sum, long max, long min) {

    this.startTime = startTime;
    this.count = count;
    this.sum = sum;
    this.max = max;
    this.min = min;
    this.mean = calcMean(count, sum);

    long millis = System.currentTimeMillis() - startTime;
    duration = Math.round(millis / 1000d);
  }

  public String toString() {
    return "count:" + count + " sum:" + sum + " min:" + min + " max:" + max;
  }

  private double calcMean(long count, double sum) {
    if (count > 0) {
      return sum / (double) count;
    }
    return 0.0;
  }

  @Override
  public long getStartTime() {
    return startTime;
  }

  public long getDuration() {
    return duration;
  }

  @Override
  public long getCount() {
    return count;
  }

  @Override
  public long getSum() {
    return sum;
  }

  @Override
  public long getMax() {
    return max;
  }

  @Override
  public long getMin() {
    return min;
  }

  @Override
  public double getMean() {
    return mean;
  }

}