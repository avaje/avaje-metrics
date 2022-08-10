package io.avaje.metrics.stats;

import io.avaje.metrics.GaugeDouble;
import io.avaje.metrics.Metric;

public final class GaugeDoubleStats implements GaugeDouble.Stats {

  private final String name;
  private final double value;

  public GaugeDoubleStats(String name, double value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public void visit(Metric.Visitor visitor) {
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
