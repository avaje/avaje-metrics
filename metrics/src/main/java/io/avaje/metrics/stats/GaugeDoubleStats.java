package io.avaje.metrics.stats;

import io.avaje.metrics.GaugeDouble;
import io.avaje.metrics.Metric;

import static java.util.Objects.requireNonNull;

public final class GaugeDoubleStats implements GaugeDouble.Stats {

  private final Metric.ID id;
  private final String unit;
  private final double value;

  public GaugeDoubleStats(Metric.ID id, double value) {
    this(id, "", value);
  }

  public GaugeDoubleStats(Metric.ID id, String unit, double value) {
    this.id = id;
    this.unit = normalizeUnit(unit);
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
  public String unit() {
    return unit;
  }

  @Override
  public double value() {
    return value;
  }

  private static String normalizeUnit(String unit) {
    var normalized = requireNonNull(unit, "unit");
    return normalized.isBlank() ? "" : normalized;
  }
}
