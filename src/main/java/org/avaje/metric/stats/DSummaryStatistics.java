package org.avaje.metric.stats;

import java.util.concurrent.TimeUnit;

import org.avaje.metric.ValueStatistics;

public class DSummaryStatistics implements ValueStatistics {

  final TimeUnit rateUnit;
  final long startTime;
  final long count;
  final double sum;
  final double max;
  final double min;
  final double mean;

  final long duration;
  final double eventRate;
  final double loadRate;

  public DSummaryStatistics() {
    this.rateUnit = null;
    this.startTime = System.currentTimeMillis();
    this.count = 0;
    this.sum = 0;
    this.max = Long.MIN_VALUE;
    this.min = Long.MAX_VALUE;
    this.mean = 0;
    this.duration = 0;
    this.eventRate = 0;
    this.loadRate = 0;
  }

  public DSummaryStatistics(TimeUnit rateUnit, long startTime, long count, double sum, double max, double min) {
    this.rateUnit = rateUnit;
    this.startTime = startTime;
    this.count = count;
    this.sum = sum;
    this.max = max;
    this.min = min;
    this.mean = calcMean(count, sum);

    long millis = System.currentTimeMillis() - startTime;
    duration = Math.round(millis / 1000d);

    double millisRate = (millis) * (double) rateUnit.toSeconds(1);

    eventRate = (count == 0) ? 0d : count * 1000d / millisRate;
    loadRate = (count == 0) ? 0d : sum * 1000d / millisRate;
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

  public double getEventRate() {
    return eventRate;
  }

  public double getLoadRate() {
    return loadRate;
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