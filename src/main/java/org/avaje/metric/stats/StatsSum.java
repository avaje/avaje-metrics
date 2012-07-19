package org.avaje.metric.stats;

import org.avaje.metric.Stats;

public class StatsSum implements Stats.Summary {

  final long startTime;
  final long count;
  final double sum;
  final double max;
  final double min;
  final double mean;

  public StatsSum() {
    this.startTime = System.currentTimeMillis();
    this.count = 0;
    this.sum = 0;
    this.max = Long.MIN_VALUE;
    this.min = Long.MAX_VALUE;
    this.mean = 0;
  }

  public StatsSum(long startTime, long count, double sum, double max, double min) {
    this.startTime = startTime;
    this.count = count;
    this.sum = sum;
    this.max = max;
    this.min = min;
    this.mean = calcMean(count, sum);
  }
  
  public StatsSum(Stats.Summary s) {
    this.startTime = s.getStartTime();
    this.count = s.getCount();
    this.sum = s.getSum();
    this.max = s.getMax();
    this.min = s.getMin();
    this.mean = s.getMean();
  }

  public String toString() {
    return "count:"+count+" sum:"+sum+" min:"+min+" max:"+max;
  }
  
  private double calcMean(long count, double sum) {
    if (count > 0) {
      return sum / (double) count;
    }
    return 0.0;
  }
  
  public StatsSum merge(Stats.Summary s) {
    if (s.getCount() == 0) {
      return this;
    }
    long newCount = count + s.getCount();
    double newSum = sum + s.getSum();
    double newMin = Math.min(min, s.getMin());
    double newMax = Math.max(max, s.getMax()); 
    long newStartTime = Math.min(startTime, s.getStartTime());
   
    return new StatsSum(newStartTime, newCount, newSum, newMax, newMin);
  }

  public long getSinceSeconds() {
    return (System.currentTimeMillis() - startTime)/1000;
  }
  
  @Override
  public long getStartTime() {
    return startTime;
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