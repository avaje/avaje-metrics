package org.avaje.metric.stats;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.avaje.metric.Clock;
import org.avaje.metric.MetricValueEvent;
import org.avaje.metric.MetricPercentiles;
import org.avaje.metric.MetricStatistics;


/**
 * Default implementation of MetricStatistics.
 */
public class MetricStatsCollector implements MetricStatistics {
  
  private final Histogram histogram;
  private final RateMeter rateMeter;
  
  public MetricStatsCollector(TimeUnit rateUnit, Clock clock) {
    this.rateMeter = new RateMeter("calls", rateUnit, clock);
    this.histogram = new Histogram();
  }
  
  public void clear() {
    rateMeter.clear();
    histogram.clear();
  }
  
  public void update(List<? extends MetricValueEvent> events) {
     
    histogram.update(events);
    rateMeter.updateAndTick(events.size());
  }
  
  public String toString() {
    return "count:"+getCount()
        +" min:"+getMin()+" max:"+getMax()+" sum:"+getSum()+" mean:"+getMean()+" std:"+getStdDev()
        +" 15min:"+getFifteenMinuteRate()+" 5min:"+getFiveMinuteRate()+" 1min:"+getOneMinuteRate()+" meanRate:"+getMeanRate()
        +" "+getPercentiles().toString();
        
  }
  

  @Override
  public long getCount() {
      return histogram.getCount();
  }

  @Override
  public double getFifteenMinuteRate() {
      return rateMeter.getFifteenMinuteRate();
  }

  @Override
  public double getFiveMinuteRate() {
      return rateMeter.getFiveMinuteRate();
  }

  @Override
  public double getMeanRate() {
      return rateMeter.getMeanRate();
  }

  @Override
  public double getOneMinuteRate() {
      return rateMeter.getOneMinuteRate();
  }

  @Override
  public double getMax() {
      return convertFromNS(histogram.getMax());
  }

  @Override
  public double getMin() {
      return convertFromNS(histogram.getMin());
  }


  @Override
  public double getMean() {
      return convertFromNS(histogram.getMean());
  }


  @Override
  public double getStdDev() {
      return convertFromNS(histogram.getStdDev());
  }


  @Override
  public double getSum() {
      return convertFromNS(histogram.getSum());
  }


  @Override
  public MetricPercentiles getPercentiles() {
    return histogram.getSnapshot();
//      final double[] values = histogram.getSnapshot().getValues();
//      final double[] converted = new double[values.length];
//      for (int i = 0; i < values.length; i++) {
//          converted[i] = convertFromNS(values[i]);
//      }
//      return new Snapshot(converted);
  }
  

  private double convertFromNS(double ns) {
    return ns;
    //return ns / TimeUnit.NANOSECONDS.convert(1, durationUnit);
  }
  
}
