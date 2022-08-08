package io.avaje.metrics.supplier;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.MetricStatsVisitor;

public class GaugeLongStats implements GaugeLong.Stats {

  private final String name;
  private final long value;

  public GaugeLongStats(String name, long value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public long value() {
    return value;
  }

  @Override
  public void visit(MetricStatsVisitor reporter) {
    reporter.visit(this);
  }
}
