package org.avaje.metric.core.noop;

import org.avaje.metric.CounterMetric;
import org.avaje.metric.CounterStatistics;
import org.avaje.metric.MetricName;
import org.avaje.metric.MetricVisitor;

public final class NoopCounterMetric implements CounterMetric {

  private static final NoopCounterStatistics NOOP_STATS = NoopCounterStatistics.INSTANCE;

  protected final MetricName metricName;

  public NoopCounterMetric(MetricName metricName) {
    this.metricName = metricName;
  }
  
  @Override
  public MetricName getName() {
    return metricName;
  }

  @Override
  public boolean collectStatistics() {
    return false;
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
  public CounterStatistics getStatistics(boolean reset) {
    return NOOP_STATS;
  }

  @Override
  public CounterStatistics getCollectedStatistics() {
    return NOOP_STATS;
  }

  @Override
  public void markEvent() {
    // do nothing
  }

  @Override
  public void markEvents(long numberOfEventsOccurred) {
    // do nothing
  }

  
}
