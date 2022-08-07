package io.avaje.metrics.core.noop;

import io.avaje.metrics.Meter;
import io.avaje.metrics.MetricStatsVisitor;

/**
 * A NOOP placeholder for ValueStatistics.
 */
class NoopMeterStats implements Meter.Stats {

  static NoopMeterStats INSTANCE = new NoopMeterStats();

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

  @Override
  public String name() {
    return null;
  }

  @Override
  public void visit(MetricStatsVisitor visitor) {
    // do nothing
  }
}
