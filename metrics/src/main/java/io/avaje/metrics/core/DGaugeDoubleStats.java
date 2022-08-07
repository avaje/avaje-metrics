package io.avaje.metrics.core;


import io.avaje.metrics.GaugeDouble;
import io.avaje.metrics.MetricStatsVisitor;

final class DGaugeDoubleStats implements GaugeDouble.Stats {

  private final String name;
  private final double value;

  DGaugeDoubleStats(String name, double value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public void visit(MetricStatsVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public double value() {
    return value;
  }
}
