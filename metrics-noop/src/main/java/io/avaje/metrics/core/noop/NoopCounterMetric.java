package io.avaje.metrics.core.noop;

import io.avaje.metrics.CounterMetric;
import io.avaje.metrics.MetricStatsVisitor;

final class NoopCounterMetric implements CounterMetric {

  private static final NoopCounterStatistics NOOP_STATS = NoopCounterStatistics.INSTANCE;

  protected final String metricName;

  NoopCounterMetric(String metricName) {
    this.metricName = metricName;
  }

  @Override
  public String name() {
    return metricName;
  }

  @Override
  public void collect(MetricStatsVisitor visitor) {
    // do nothing
  }

  @Override
  public void reset() {
    // do nothing
  }

  @Override
  public void inc() {
    // do nothing
  }

  @Override
  public void inc(long numberOfEventsOccurred) {
    // do nothing
  }

  @Override
  public long count() {
    return 0;
  }
}
