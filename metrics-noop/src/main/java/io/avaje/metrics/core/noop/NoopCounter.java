package io.avaje.metrics.core.noop;

import io.avaje.metrics.Counter;
import io.avaje.metrics.MetricStatsVisitor;

final class NoopCounter implements Counter {

  private static final NoopCounterStats NOOP_STATS = NoopCounterStats.INSTANCE;

  protected final String metricName;

  NoopCounter(String metricName) {
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
