package io.avaje.metrics.core.noop;

import io.avaje.metrics.Counter;
import io.avaje.metrics.MetricStatsVisitor;

final class NoopCounterStats implements Counter.Stats {

  static final NoopCounterStats INSTANCE = new NoopCounterStats();

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
