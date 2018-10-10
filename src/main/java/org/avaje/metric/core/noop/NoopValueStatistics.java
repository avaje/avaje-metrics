package org.avaje.metric.core.noop;

//import org.avaje.metric.ValueStatistics;

import org.avaje.metric.MetricName;
import org.avaje.metric.statistics.MetricStatisticsVisitor;
import org.avaje.metric.statistics.ValueStatistics;

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
  public MetricName getName() {
    return null;
  }

  @Override
  public void visit(MetricStatisticsVisitor visitor) {
    // do nothing
  }
}
