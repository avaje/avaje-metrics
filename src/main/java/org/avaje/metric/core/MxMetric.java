package org.avaje.metric.core;

import org.avaje.metric.MetricPercentiles;
import org.avaje.metric.MetricStatistics;

class MxMetric implements MetricMXBean {

  private final MetricStatistics metricStatistics;
  
  public MxMetric(MetricStatistics metricStatistics) {
    this.metricStatistics = metricStatistics;
  }
  
  @Override
  public long getCount() {
    return metricStatistics.getCount();
  }

  @Override
  public double getFifteenMinuteRate() {
    return metricStatistics.getFifteenMinuteRate();
  }

  @Override
  public double getFiveMinuteRate() {
    return metricStatistics.getFiveMinuteRate();
  }

  @Override
  public double getMeanRate() {
    return metricStatistics.getMeanRate();
  }

  @Override
  public double getOneMinuteRate() {
    return metricStatistics.getOneMinuteRate();
  }

  @Override
  public double getMax() {
    return metricStatistics.getMax();
  }

  @Override
  public double getMin() {
    return metricStatistics.getMin();
  }

  @Override
  public double getMean() {
    return metricStatistics.getMean();
  }

  @Override
  public double getStdDev() {
    return metricStatistics.getStdDev();
  }

  @Override
  public double getSum() {
    return metricStatistics.getSum();
  }

  @Override
  public MetricPercentiles getPercentiles() {
    return metricStatistics.getPercentiles();
  }
}
