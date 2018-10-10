package org.avaje.metric.core;


import org.avaje.metric.MetricName;
import org.avaje.metric.statistics.GaugeDoubleStatistics;
import org.avaje.metric.statistics.MetricStatisticsVisitor;

class DGaugeDoubleStatistic implements GaugeDoubleStatistics {

  private final MetricName name;
  private final double value;

  DGaugeDoubleStatistic(MetricName name, double value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public String toString() {
    return name.toString();
  }

  @Override
  public void visit(MetricStatisticsVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public MetricName getName() {
    return name;
  }

  @Override
  public long getStartTime() {
    return 0;
  }

  @Override
  public double getValue() {
    return value;
  }
}
