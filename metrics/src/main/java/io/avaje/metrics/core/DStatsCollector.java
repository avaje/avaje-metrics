package io.avaje.metrics.core;

import io.avaje.metrics.*;

import java.util.ArrayList;
import java.util.List;

final class DStatsCollector implements MetricStatsVisitor {

  private final List<MetricStats> list = new ArrayList<>();

  List<MetricStats> getList() {
    return list;
  }

  @Override
  public void visit(TimedMetric.Stats metric) {
    list.add(metric);
  }

  @Override
  public void visit(ValueMetric.Stats metric) {
    list.add(metric);
  }

  @Override
  public void visit(CounterMetric.Stats metric) {
    list.add(metric);
  }

  @Override
  public void visit(GaugeDoubleMetric.Stats metric) {
    list.add(metric);
  }

  @Override
  public void visit(GaugeLongMetric.Stats metric) {
    list.add(metric);
  }

  void addAll(List<MetricStats> metrics) {
    list.addAll(metrics);
  }
}
