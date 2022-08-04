package io.avaje.metrics.core;


import io.avaje.metrics.GaugeDoubleMetric;
import io.avaje.metrics.MetricName;
import io.avaje.metrics.MetricStatsVisitor;

final class DGaugeDoubleStatistic implements GaugeDoubleMetric.Stats {

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
  public void visit(MetricStatsVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String name() {
    return name.simpleName();
  }

  @Override
  public double value() {
    return value;
  }
}
