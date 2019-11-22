package io.avaje.metrics.core.noop;

import io.avaje.metrics.statistics.MetricStatisticsVisitor;
import io.avaje.metrics.statistics.ValueStatistics;

/**
 * A NOOP placeholder for ValueStatistics.
 */
class NoopValueStatistics implements ValueStatistics {

  static NoopValueStatistics INSTANCE = new NoopValueStatistics();

  @Override
  public long getStartTime() {
    return 0;
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

  @Override
  public String getName() {
    return null;
  }

  @Override
  public void visit(MetricStatisticsVisitor visitor) {
    // do nothing
  }
}
