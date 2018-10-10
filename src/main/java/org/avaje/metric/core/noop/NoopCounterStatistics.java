package org.avaje.metric.core.noop;

import org.avaje.metric.MetricName;
import org.avaje.metric.statistics.CounterStatistics;
import org.avaje.metric.statistics.MetricStatisticsVisitor;

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
  public MetricName getName() {
    return null;
  }
}
