package io.avaje.metrics.stats;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.Metric;

public final class GaugeLongStats implements GaugeLong.Stats {

  private final Metric.ID id;
  private final long value;

  public GaugeLongStats(Metric.ID id, long value) {
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
  public long value() {
    return value;
  }
}
