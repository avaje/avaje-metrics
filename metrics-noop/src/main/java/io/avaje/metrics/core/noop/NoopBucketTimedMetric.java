package io.avaje.metrics.core.noop;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.TimedEvent;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.statistics.MetricStatisticsVisitor;

import java.util.Map;
import java.util.function.Supplier;

class NoopBucketTimedMetric implements TimedMetric {

  private static final int[] noRange = {};

  private static final TimedMetric[] noBuckets = new TimedMetric[0];

  private static final NoopTimedEvent NOOP_TIMED_EVENT = new NoopTimedEvent();

  private final MetricName name;


  NoopBucketTimedMetric(MetricName name) {
    this.name = name;
  }

  @Override
  public MetricName getName() {
    return name;
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
  public void collect(MetricStatisticsVisitor visitor) {
    // do nothing
  }

  @Override
  public void clear() {
    // do nothing
  }

  @Override
  public void time(Runnable event) {
    event.run();
  }

  @Override
  public <T> T time(Supplier<T> event) {
    return event.get();
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
  public void add(long startNanos) {
    // do nothing
  }

  @Override
  public void add(long startNanos, boolean activeThreadContext) {
    // do nothing
  }

  @Override
  public void addErr(long startNanos) {
    // do nothing
  }

  @Override
  public void addErr(long startNanos, boolean activeThreadContext) {
    // do nothing
  }

  @Override
  public void setRequestTiming(int collectionCount) {
    // do nothing
  }

  @Override
  public int getRequestTiming() {
    return 0;
  }

  @Override
  public void decrementRequestTiming() {

  }

  @Override
  public boolean isRequestTiming() {
    return false;
  }

  @Override
  public Map<String, String> attributes() {
    return null;
  }
}
