package io.avaje.metrics.core.noop;

import io.avaje.metrics.MetricStatsVisitor;
import io.avaje.metrics.Timer;

import java.util.function.Supplier;

class NoopBucketTimer implements Timer {

  private static final NoopTimerEvent NOOP_TIMED_EVENT = new NoopTimerEvent();

  private final String name;

  NoopBucketTimer(String name) {
    this.name = name;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String bucketRange() {
    return "";
  }

  @Override
  public void collect(MetricStatsVisitor visitor) {
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
    // do nothing
  }

//  @Override
//  public void add(long startNanos, boolean activeThreadContext) {
//    // do nothing
//  }

  @Override
  public void addErr(long startNanos) {
    // do nothing
  }

//  @Override
//  public void addErr(long startNanos, boolean activeThreadContext) {
//    // do nothing
//  }

//  @Override
//  public void setRequestTiming(int collectionCount) {
//    // do nothing
//  }
//
//  @Override
//  public int getRequestTiming() {
//    return 0;
//  }
//
//  @Override
//  public void decrementRequestTiming() {
//
//  }
//
//  @Override
//  public boolean isRequestTiming() {
//    return false;
//  }
//
//  @Override
//  public Map<String, String> attributes() {
//    return null;
//  }
}
