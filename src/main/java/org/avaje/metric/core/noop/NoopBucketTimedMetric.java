package org.avaje.metric.core.noop;

import org.avaje.metric.BucketTimedMetric;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;
import org.avaje.metric.MetricVisitor;
import org.avaje.metric.TimedEvent;
import org.avaje.metric.TimedMetric;

import java.util.List;
import java.util.Map;

public class NoopBucketTimedMetric implements BucketTimedMetric {

  private static final int[] noRange = {};

  private static final TimedMetric[] noBuckets = new TimedMetric[0];
  
  private static final NoopTimedEvent NOOP_TIMED_EVENT = new NoopTimedEvent();
  
  private final MetricName name;

  
  public NoopBucketTimedMetric(MetricName name) {
    this.name = name;
  }
  
  @Override
  public MetricName getName() {
    return name;
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
  public int[] getBucketRanges() {
    return noRange;
  }

  @Override
  public TimedMetric[] getBuckets() {
    return noBuckets;
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
  public void operationEnd(int opCode, long startNanos, boolean useContext) {
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

  }

  @Override
  public boolean isActiveThreadContext() {
    return false;
  }

  @Override
  public Map<String, String> attributes() {
    return null;
  }
}
