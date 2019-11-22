package io.avaje.metrics.core.noop;

import io.avaje.metrics.statistics.CounterStatistics;
import io.avaje.metrics.statistics.MetricStatisticsVisitor;

final class NoopCounterStatistics implements CounterStatistics {

  static final NoopCounterStatistics INSTANCE = new NoopCounterStatistics();

  @Override
  public void visit(MetricStatisticsVisitor visitor) {

  }

  @Override
  public long getStartTime() {
    return 0;
  }

  @Override
  public long getCount() {
    return 0;
  }

  @Override
  public String getName() {
    return null;
  }
}
