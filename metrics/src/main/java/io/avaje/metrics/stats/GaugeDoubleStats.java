package io.avaje.metrics.stats;

import io.avaje.metrics.GaugeDouble;
import io.avaje.metrics.Metric;

public final class GaugeDoubleStats implements GaugeDouble.Stats {

  private final Metric.ID id;
  private final double value;

  public GaugeDoubleStats(Metric.ID id, double value) {
    this.id = id;
    this.value = value;
  }

  @Override
  public String toString() {
    return id.toString();
  }

  @Override
  public void visit(Metric.Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public Metric.ID id() {
    return id;
  }

  @Override
  public String name() {
    return id.name();
  }

  @Override
  public String[] tags() {
    return id.tags().array();
  }

  @Override
  public double value() {
    return value;
  }
}
