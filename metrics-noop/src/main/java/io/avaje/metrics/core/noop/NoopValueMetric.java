package io.avaje.metrics.core.noop;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.ValueMetric;
import io.avaje.metrics.statistics.MetricStatisticsVisitor;

class NoopValueMetric implements ValueMetric {

  private static final NoopValueStatistics NOOP_STATS = NoopValueStatistics.INSTANCE;

  private final MetricName metricName;

  NoopValueMetric(MetricName metricName) {
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
  public void addEvent(long value) {
    // do nothing
  }

  @Override
  public long getCount() {
    return 0;
  }

  @Override
  public long getTotal() {
    return 0;
  }

  @Override
  public long getMax() {
    return 0;
  }

  @Override
  public long getMean() {
    return 0;
  }
}
