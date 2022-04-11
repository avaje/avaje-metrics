package io.avaje.metrics.core;

import io.avaje.metrics.statistics.*;

import java.util.ArrayList;
import java.util.List;

class HelperStatsCollector implements MetricStatisticsVisitor {

  private final List<MetricStatistics> list = new ArrayList<>();


  public List<MetricStatistics> getList() {
    return list;
  }

  @Override
  public void visit(TimedStatistics metric) {
    list.add(metric);
  }

  @Override
  public void visit(ValueStatistics metric) {
    list.add(metric);
  }

  @Override
  public void visit(CounterStatistics metric) {
    list.add(metric);
  }

  @Override
  public void visit(GaugeDoubleStatistics metric) {
    list.add(metric);
  }

  @Override
  public void visit(GaugeLongStatistics metric) {
    list.add(metric);
  }

}
