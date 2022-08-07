package io.avaje.metrics.core;

import io.avaje.metrics.*;
import io.avaje.metrics.Counter;

import java.util.ArrayList;
import java.util.List;

final class DStatsCollector implements MetricStatsVisitor {

  private final List<MetricStats> list = new ArrayList<>();

  List<MetricStats> list() {
    return list;
  }

  @Override
  public void visit(Timer.Stats metric) {
    list.add(metric);
  }

  @Override
  public void visit(Meter.Stats metric) {
    list.add(metric);
  }

  @Override
  public void visit(Counter.Stats metric) {
    list.add(metric);
  }

  @Override
  public void visit(GaugeDouble.Stats metric) {
    list.add(metric);
  }

  @Override
  public void visit(GaugeLong.Stats metric) {
    list.add(metric);
  }

  void addAll(List<MetricStats> metrics) {
    list.addAll(metrics);
  }
}
