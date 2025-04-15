package io.avaje.metrics.core;

import io.avaje.metrics.Timer;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Designed to capture the duration of timed events.
 * <p>
 * The major difference compared with ValueMetric is that it is specifically oriented towards
 * collecting time duration and provides separate statistics for success and error completion.
 */
final class DTimer implements Timer {

  private final String name;
  private final @Nullable String bucketRange;
  private final ValueCounter successCounter;
  private final ValueCounter errorCounter;

  DTimer(String name) {
    this.name = name;
    this.bucketRange = null;
    this.successCounter = new ValueCounter(name);
    this.errorCounter = new ValueCounter(name + ".error");
  }

  DTimer(String name, String bucketRange) {
    this.name = name;
    this.bucketRange = bucketRange;
    this.successCounter = new ValueCounter(name, bucketRange);
    this.errorCounter = new ValueCounter(name + ".error");
  }

  @Override
  public String toString() {
    return name + ":" + successCounter + ((errorCounter.count() == 0) ? "" : " error:" + errorCounter);
  }

  @Override
  public @Nullable String bucketRange() {
    return bucketRange;
  }

  @Override
  public void reset() {
    successCounter.reset();
    errorCounter.reset();
  }

  static long tickNanos() {
    return System.nanoTime();
  }

  @Override
  public void collect(Visitor collector) {
    final Stats successStats = successCounter.collect(collector);
    if (successStats != null) {
      collector.visit(successStats);
    }
    final Stats errorStats = errorCounter.collect(collector);
    if (errorStats != null) {
      collector.visit(errorStats);
    }
  }

  public String name() {
    return name;
  }

  @Override
  public void time(Runnable event) {
    long start = System.nanoTime();
    try {
      event.run();
      add(start);
    } catch (RuntimeException e) {
      addErr(start);
      throw e;
    }
  }

  @Override
  public <T> T time(Supplier<T> event) {
    long start = System.nanoTime();
    try {
      final T result = event.get();
      add(start);
      return result;
    } catch (Exception e) {
      addErr(start);
      throw e;
    }
  }

  /**
   * Start an event.
   * <p>
   * The {@link Event#end()} or {@link Event#endWithError()} are called at the
   * completion of the timed event.
   */
  @Override
  public Event startEvent() {
    return new DTimerEvent(this);
  }

  /**
   * Add an event duration in nanoseconds noting if it was a success or failure result.
   * <p>
   * Success and failure statistics are kept separately.
   */
  @Override
  public void addEventDuration(boolean success, long durationNanos) {
    if (success) {
      successCounter.add(TimeUnit.NANOSECONDS.toMicros(durationNanos));
    } else {
      errorCounter.add(TimeUnit.NANOSECONDS.toMicros(durationNanos));
    }
  }

  /**
   * Add an event with duration calculated based on startNanos.
   */
  @Override
  public void addEventSince(boolean success, long startNanos) {
    addEventDuration(success, System.nanoTime() - startNanos);
  }

  @Override
  public void add(long startNanos) {
    successCounter.add(TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startNanos));
  }

  @Override
  public void addErr(long startNanos) {
    errorCounter.add(TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startNanos));
  }
}
