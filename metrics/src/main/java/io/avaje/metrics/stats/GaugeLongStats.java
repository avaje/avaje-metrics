package io.avaje.metrics.stats;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.Metric;

import static java.util.Objects.requireNonNull;

public final class GaugeLongStats implements GaugeLong.Stats {

  private final Metric.ID id;
  private final String unit;
  private final long value;

  public GaugeLongStats(Metric.ID id, long value) {
    this(id, "", value);
  }

  public GaugeLongStats(Metric.ID id, String unit, long value) {
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
  public long value() {
    return value;
  }

  private static String normalizeUnit(String unit) {
    var normalized = requireNonNull(unit, "unit");
    return normalized.isBlank() ? "" : normalized;
  }
}
