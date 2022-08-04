package io.avaje.metrics.core;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.TimedEvent;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.MetricStatsVisitor;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Designed to capture the duration of timed events.
 * <p>
 * The major difference compared with ValueMetric is that it is specifically oriented towards
 * collecting time duration and provides separate statistics for success and error completion.
 * </p>
 */
final class DTimedMetric implements TimedMetric {

  private static final String noBuckets = "";

  private final MetricName name;
  private final String bucketRange;
  private final ValueCounter successCounter;
  private final ValueCounter errorCounter;

  DTimedMetric(MetricName name) {
    this.name = name;
    this.bucketRange = noBuckets;
    this.successCounter = new ValueCounter(name);
    this.errorCounter = new ValueCounter(name.append("error"));
  }

  DTimedMetric(MetricName name, String bucketRange) {
    this.name = name;
    this.bucketRange = bucketRange;
    this.successCounter = new ValueCounter(name, bucketRange);
    this.errorCounter = new ValueCounter(name.append("error"));
  }

  @Override
  public String toString() {
    return name.toString();
  }

  @Override
  public boolean isBucket() {
    return !noBuckets.equals(bucketRange);
  }

  @Override
  public String bucketRange() {
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
  public void collect(MetricStatsVisitor collector) {
    Stats errStats = errorCounter.collectStatistics();
    if (errStats != null) {
      collector.visit(errStats);
    }
    Stats successStats = successCounter.collectStatistics();
    if (successStats != null) {
      collector.visit(successStats);
    }
  }

  public MetricName name() {
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
   * The {@link TimedEvent#end()} or {@link TimedEvent#endWithError()} are called at the
   * completion of the timed event.
   * </p>
   */
  @Override
  public TimedEvent startEvent() {
    return new DTimedMetricEvent(this);
  }

  /**
   * Add an event duration in nanoseconds noting if it was a success or failure result.
   * <p>
   * Success and failure statistics are kept separately.
   * </p>
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
  public void add(long startNanos, boolean activeThreadContext) {
    successCounter.add(TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startNanos));
//    if (activeThreadContext) {
//      NestedContext.pop();
//    }
  }

  @Override
  public void addErr(long startNanos) {
    errorCounter.add(TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startNanos));
  }

  @Override
  public void addErr(long startNanos, boolean activeThreadContext) {
    errorCounter.add(TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startNanos));
//    if (activeThreadContext) {
//      NestedContext.pop();
//    }
  }
}
