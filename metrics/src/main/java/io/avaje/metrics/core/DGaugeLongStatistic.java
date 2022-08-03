package io.avaje.metrics.core;


import io.avaje.metrics.MetricName;
import io.avaje.metrics.statistics.GaugeLongStatistics;
import io.avaje.metrics.statistics.MetricStatisticsVisitor;

final class DGaugeLongStatistic implements GaugeLongStatistics {

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
  public void visit(MetricStatisticsVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String getName() {
    return name.getSimpleName();
  }

  @Override
  public long getStartTime() {
    return 0;
  }

  @Override
  public long getValue() {
    return value;
  }
}
