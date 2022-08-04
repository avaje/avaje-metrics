package io.avaje.metrics.core.noop;

import io.avaje.metrics.CounterMetric;
import io.avaje.metrics.MetricStatsVisitor;

final class NoopCounterStatistics implements CounterMetric.Stats {

  static final NoopCounterStatistics INSTANCE = new NoopCounterStatistics();

  @Override
  public void visit(MetricStatsVisitor visitor) {

  }

  @Override
  public long count() {
    return 0;
  }

  @Override
  public String name() {
    return null;
  }
}
