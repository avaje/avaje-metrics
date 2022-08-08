package io.avaje.metrics.core;

import io.avaje.metrics.*;
import io.avaje.metrics.Counter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

final class DStatsCollector implements MetricStatsVisitor {

  private final List<MetricStats> list = new ArrayList<>();
  private final  Function<String, String> namingConvention;

  public DStatsCollector(Function<String, String> namingConvention) {
    this.namingConvention = namingConvention;
  }

  List<MetricStats> list() {
    return list;
  }

  @Override
  public Function<String, String> namingConvention() {
    return namingConvention;
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
