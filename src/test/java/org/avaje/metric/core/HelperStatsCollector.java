package org.avaje.metric.core;

import org.avaje.metric.statistics.CounterStatistics;
import org.avaje.metric.statistics.GaugeDoubleStatistics;
import org.avaje.metric.statistics.GaugeLongStatistics;
import org.avaje.metric.statistics.MetricStatistics;
import org.avaje.metric.statistics.MetricStatisticsVisitor;
import org.avaje.metric.statistics.TimedStatistics;
import org.avaje.metric.statistics.ValueStatistics;

import java.util.ArrayList;
import java.util.List;

public class HelperStatsCollector implements MetricStatisticsVisitor {

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
