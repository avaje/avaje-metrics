package io.avaje.metrics.core.noop;

import io.avaje.metrics.MetricStatsVisitor;
import io.avaje.metrics.Meter;

class NoopValueMetric implements Meter {

  private static final NoopValueStatistics NOOP_STATS = NoopValueStatistics.INSTANCE;

  private final String metricName;

  NoopValueMetric(String metricName) {
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
  public void addEvent(long value) {
    // do nothing
  }

  @Override
  public long count() {
    return 0;
  }

  @Override
  public long total() {
    return 0;
  }

  @Override
  public long max() {
    return 0;
  }

  @Override
  public long mean() {
    return 0;
  }
}
