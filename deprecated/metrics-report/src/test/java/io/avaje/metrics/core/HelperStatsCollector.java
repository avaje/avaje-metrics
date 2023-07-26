package io.avaje.metrics.core;

import io.avaje.metrics.*;
import io.avaje.metrics.Counter;

import java.util.ArrayList;
import java.util.List;

class HelperStatsCollector implements Metric.Visitor {

  private final List<Metric.Statistics> list = new ArrayList<>();


  public List<Metric.Statistics> getList() {
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

}
