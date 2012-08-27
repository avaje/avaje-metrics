package org.avaje.metric.stats;

import org.avaje.metric.ValueStatistics;

public class DSummaryStatistics implements ValueStatistics {

  private final long startTime;
  private final long count;
  private final double sum;
  private final double max;
  private final double min;
  private final double mean;

  private final long duration;

  public DSummaryStatistics() {
    this.startTime = System.currentTimeMillis();
    this.count = 0;
    this.sum = 0;
    this.max = Long.MIN_VALUE;
    this.min = Long.MAX_VALUE;
    this.mean = 0;
    this.duration = 0;
  }

  public DSummaryStatistics(long startTime, long count, double sum, double max, double min) {

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
  public double getSum() {
    return sum;
  }

  @Override
  public double getMax() {
    return max;
  }

  @Override
  public double getMin() {
    return min;
  }

  @Override
  public double getMean() {
    return mean;
  }

}