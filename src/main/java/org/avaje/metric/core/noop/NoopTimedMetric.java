package org.avaje.metric.core.noop;

import org.avaje.metric.MetricName;
import org.avaje.metric.TimedEvent;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.statistics.MetricStatisticsVisitor;

import java.util.Map;

public class NoopTimedMetric implements TimedMetric {

  private static final NoopTimedEvent NOOP_TIMED_EVENT = new NoopTimedEvent();

  private static final NoopValueStatistics NOOP_STATS = NoopValueStatistics.INSTANCE;

  private final MetricName metricName;

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
  public void collect(MetricStatisticsVisitor metricStatisticsVisitor) {
    // do nothing
  }

  @Override
  public void clear() {
    // do nothing
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
  public void operationEnd(int opCode, long startNanos) {

  }

  @Override
  public Map<String, String> attributes() {
    return null;
  }
}
