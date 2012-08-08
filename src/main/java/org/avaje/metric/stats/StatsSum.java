package org.avaje.metric.stats;

import java.util.concurrent.TimeUnit;

import org.avaje.metric.Stats;

public class StatsSum implements Stats.Summary {

  final TimeUnit rateUnit;
  final long startTime;
  final long count;
  final double sum;
  final double max;
  final double min;
  final double mean;
  
  long duration;
  double eventRate;
  double loadRate;
  

  public StatsSum() {
    this.rateUnit = null;
    this.startTime = System.currentTimeMillis();
    this.count = 0;
    this.sum = 0;
    this.max = Long.MIN_VALUE;
    this.min = Long.MAX_VALUE;
    this.mean = 0;
  }

  public StatsSum(TimeUnit rateUnit, long startTime, long count, double sum, double max, double min) {
    this.rateUnit = rateUnit;
    this.startTime = startTime;
    this.count = count;
    this.sum = sum;
    this.max = max;
    this.min = min;
    this.mean = calcMean(count, sum);
    
    long millis = System.currentTimeMillis() - startTime;
    duration =  Math.round(millis / 1000d);
    
    double millisRate = (millis) * (double) rateUnit.toSeconds(1);
    
    eventRate = (count == 0) ? 0d : count * 1000d / millisRate;    
    loadRate = (count == 0) ? 0d : sum * 1000d / millisRate; 
  }

//  public StatsSum(Stats.Summary s) {
//    this.startTime = s.getStartTime();
//    this.count = s.getCount();
//    this.sum = s.getSum();
//    this.max = s.getMax();
//    this.min = s.getMin();
//    this.mean = s.getMean();
//  }

  public String toString() {
    return "count:" + count + " sum:" + sum + " min:" + min + " max:" + max;
  }

  private double calcMean(long count, double sum) {
    if (count > 0) {
      return sum / (double) count;
    }
    return 0.0;
  }

//  public StatsSum merge(Stats.Summary s) {
//    if (s == null || s.getCount() == 0) {
//      return this;
//    }
//    long newCount = count + s.getCount();
//    double newSum = sum + s.getSum();
//    double newMin = Math.min(min, s.getMin());
//    double newMax = Math.max(max, s.getMax());
//    long newStartTime = Math.min(startTime, s.getStartTime());
//
//    return new StatsSum(rateUnit, newStartTime, newCount, newSum, newMax, newMin);
//  }

  public long getDuration() {
    return duration;// Math.round((System.currentTimeMillis() - startTime) / 1000d);
  }

//  public double getEventRate() {
//    if (count == 0) {
//      return 0;
//    }
//    long millis = (System.currentTimeMillis() - startTime);
//    return count * 1000d / millis;
//  }
  
  public double getEventRate() {
    return eventRate;
//    if (count == 0) {
//      return 0d;
//    }
//    return count * 1000d / (System.currentTimeMillis() - startTime) * (double) rateUnit.toSeconds(1);    
  }
  
  public double getLoadRate() {
    return loadRate;
//    if (count == 0) {
//      return 0d;
//    }
//    return sum * 1000d / (System.currentTimeMillis() - startTime) * (double) rateUnit.toSeconds(1);    
  }
  
  //double rate = summary.getEventRate(); 
  
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