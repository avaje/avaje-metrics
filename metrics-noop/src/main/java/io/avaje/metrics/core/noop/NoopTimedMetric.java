package io.avaje.metrics.core.noop;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.TimedEvent;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.MetricStatsVisitor;

import java.util.Map;
import java.util.function.Supplier;

public class NoopTimedMetric implements TimedMetric {

  private static final NoopTimedEvent NOOP_TIMED_EVENT = new NoopTimedEvent();

  private static final NoopValueStatistics NOOP_STATS = NoopValueStatistics.INSTANCE;

  private final MetricName metricName;

  public NoopTimedMetric(MetricName metricName) {
    this.metricName = metricName;
  }

  @Override
  public MetricName name() {
    return metricName;
  }

  @Override
  public boolean isBucket() {
    return false;
  }

  @Override
  public String bucketRange() {
    return "";
  }

  @Override
  public void collect(MetricStatsVisitor metricStatisticsVisitor) {
    // do nothing
  }

  @Override
  public void reset() {
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
  public boolean isRequestTiming() {
    return false;
  }

  @Override
  public void add(long startNanos) {

  }

  @Override
  public void add(long startNanos, boolean activeThreadContext) {

  }

  @Override
  public void addErr(long startNanos) {

  }

  @Override
  public void addErr(long startNanos, boolean activeThreadContext) {

  }

  @Override
  public Map<String, String> attributes() {
    return null;
  }
}
