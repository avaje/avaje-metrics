package io.avaje.metrics.core;


import io.avaje.metrics.MetricName;
import io.avaje.metrics.statistics.GaugeDoubleStatistics;
import io.avaje.metrics.statistics.MetricStatisticsVisitor;

final class DGaugeDoubleStatistic implements GaugeDoubleStatistics {

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
  public String getName() {
    return name.getSimpleName();
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
