package io.avaje.metrics.core.noop;

import io.avaje.metrics.Timer;

import java.util.function.Supplier;

public class NoopTimer implements Timer {

  private static final NoopTimerEvent NOOP_TIMED_EVENT = new NoopTimerEvent();

  private final String metricName;

  public NoopTimer(String metricName) {
    this.metricName = metricName;
  }

  @Override
  public String name() {
    return metricName;
  }

  @Override
  public String bucketRange() {
    return "";
  }

  @Override
  public void collect(Visitor metricStatisticsVisitor) {
    // do nothing
  }

  @Override
  public void reset() {
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
  public Event startEvent() {
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

  }

  @Override
  public void addErr(long startNanos) {

  }
}
