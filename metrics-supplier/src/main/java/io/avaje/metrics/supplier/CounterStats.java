package io.avaje.metrics.supplier;

import io.avaje.metrics.Counter;
import io.avaje.metrics.MetricStatsVisitor;

public class CounterStats implements Counter.Stats {

  final String name;
  final long count;

  public CounterStats(String name, long count) {
    this.name = name;
    this.count = count;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public long count() {
    return count;
  }

  @Override
  public void visit(MetricStatsVisitor reporter) {
    reporter.visit(this);
  }
}
