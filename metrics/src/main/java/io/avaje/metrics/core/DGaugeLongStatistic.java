package io.avaje.metrics.core;


import io.avaje.metrics.GaugeLongMetric;
import io.avaje.metrics.MetricName;
import io.avaje.metrics.MetricStatsVisitor;

final class DGaugeLongStatistic implements GaugeLongMetric.Stats {

  private final MetricName name;
  private final long value;

  DGaugeLongStatistic(MetricName name, long value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public String toString() {
    return name.toString();
  }

  @Override
  public void visit(MetricStatsVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String name() {
    return name.simpleName();
  }

  @Override
  public long value() {
    return value;
  }
}
