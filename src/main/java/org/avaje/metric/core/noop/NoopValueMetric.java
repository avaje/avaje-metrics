package org.avaje.metric.core.noop;

import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;
import org.avaje.metric.MetricVisitor;
import org.avaje.metric.ValueMetric;
import org.avaje.metric.ValueStatistics;

import java.util.List;

public class NoopValueMetric implements ValueMetric {

  private static final NoopValueStatistics NOOP_STATS = NoopValueStatistics.INSTANCE;

  protected final MetricName metricName;

  public NoopValueMetric(MetricName metricName) {
    this.metricName = metricName;
  }
  
  @Override
  public MetricName getName() {
    return metricName;
  }

  @Override
  public void collectStatistics(List<Metric> list) {
    // do nothing
  }

  @Override
  public void visit(MetricVisitor visitor) {
    // do nothing
  }

  @Override
  public void clearStatistics() {
    // do nothing
  }

  @Override
  public ValueStatistics getCollectedStatistics() {
    return NOOP_STATS;
  }

  @Override
  public void addEvent(long value) {
    // do nothing
  }

  
}
