package io.avaje.metrics.core;

import io.avaje.metrics.*;

import java.util.ArrayList;
import java.util.List;

class HelperStatsCollector implements MetricStatsVisitor {

  private final List<MetricStats> list = new ArrayList<>();


  public List<MetricStats> getList() {
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

}
