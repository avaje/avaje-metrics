package io.avaje.metrics.supplier;

import io.avaje.metrics.GaugeDouble;
import io.avaje.metrics.MetricStatsVisitor;

public class GaugeDoubleStats implements GaugeDouble.Stats {

  private final String name;
  private final double value;

  public GaugeDoubleStats(String name, double value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public double value() {
    return value;
  }

  @Override
  public void visit(MetricStatsVisitor reporter) {
    reporter.visit(this);
  }
}
