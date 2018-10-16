package org.avaje.metric.core;


import org.avaje.metric.MetricName;
import org.avaje.metric.statistics.GaugeLongStatistics;
import org.avaje.metric.statistics.MetricStatisticsVisitor;

class DGaugeLongStatistic implements GaugeLongStatistics {

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
