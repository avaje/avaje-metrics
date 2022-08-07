package io.avaje.metrics.core;


import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.MetricStatsVisitor;

final class DGaugeLongStats implements GaugeLong.Stats {

  private final String name;
  private final long value;

  DGaugeLongStats(String name, long value) {
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
  public long value() {
    return value;
  }
}
