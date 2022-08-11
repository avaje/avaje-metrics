package io.avaje.metrics.stats;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.Metric;

public final class GaugeLongStats implements GaugeLong.Stats {

  private final String name;
  private final long value;

  public GaugeLongStats(String name, long value) {
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
  public long value() {
    return value;
  }
}
