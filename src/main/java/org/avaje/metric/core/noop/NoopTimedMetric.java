package org.avaje.metric.core.noop;

import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;
import org.avaje.metric.MetricVisitor;
import org.avaje.metric.TimedEvent;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.ValueStatistics;

import java.util.List;
import java.util.Map;

public class NoopTimedMetric implements TimedMetric {

  private static final NoopTimedEvent NOOP_TIMED_EVENT = new NoopTimedEvent();
  
  private static final NoopValueStatistics NOOP_STATS = NoopValueStatistics.INSTANCE;
  
  protected final MetricName metricName;
  
  public NoopTimedMetric(MetricName metricName) {
    this.metricName = metricName;
  }
  
  @Override
  public MetricName getName() {
    return metricName;
  }

  @Override
  public boolean isBucket() {
    return false;
  }

  @Override
  public String getBucketRange() {
    return "";
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
  public ValueStatistics getCollectedSuccessStatistics() {
    return NOOP_STATS;
  }

  @Override
  public ValueStatistics getCollectedErrorStatistics() {
    return NOOP_STATS;
  }

  @Override
  public ValueStatistics getSuccessStatistics(boolean reset) {
    return NOOP_STATS;
  }

  @Override
  public ValueStatistics getErrorStatistics(boolean reset) {
    return NOOP_STATS;
  }

  @Override
  public void setRequestTimingCollection(int collectionCount) {
    // do nothing
  }

  @Override
  public int getRequestTimingCollection() {
    return 0;
  }

  @Override
  public void decrementCollectionCount() {
    // do nothing
  }

  @Override
  public TimedEvent startEvent() {
    return NOOP_TIMED_EVENT;
  }

  @Override
  public void addEventSince(boolean success, long startNanos) {
    // do nothing
  }

  @Override
  public void addEventDuration(boolean success, long durationNanos) {
    // do nothing
  }

  @Override
  public boolean isActiveThreadContext() {
    return false;
  }

  @Override
  public void operationEnd(int opCode, long startNanos, boolean activeThreadContext) {
    // do nothing    
  }

  @Override
  public Map<String, String> attributes() {
    return null;
  }
}
