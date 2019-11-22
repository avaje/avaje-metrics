package io.avaje.metrics.core.noop;

import io.avaje.metrics.CounterMetric;
import io.avaje.metrics.MetricName;
import io.avaje.metrics.statistics.MetricStatisticsVisitor;

final class NoopCounterMetric implements CounterMetric {

  private static final NoopCounterStatistics NOOP_STATS = NoopCounterStatistics.INSTANCE;

  protected final MetricName metricName;

  NoopCounterMetric(MetricName metricName) {
    this.metricName = metricName;
  }

  @Override
  public MetricName getName() {
    return metricName;
  }

  @Override
  public void collect(MetricStatisticsVisitor visitor) {
    // do nothing
  }

  @Override
  public void clear() {
    // do nothing
  }

  @Override
  public void markEvent() {
    // do nothing
  }

  @Override
  public void markEvents(long numberOfEventsOccurred) {
    // do nothing
  }

  @Override
  public long getCount() {
    return 0;
  }
}
