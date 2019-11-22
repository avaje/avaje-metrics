package io.avaje.metrics.core;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.TimedEvent;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.statistics.MetricStatisticsVisitor;
import io.avaje.metrics.statistics.TimedStatistics;

import java.util.concurrent.TimeUnit;

/**
 * Designed to capture the duration of timed events.
 * <p>
 * The major difference compared with ValueMetric is that it is specifically oriented towards
 * collecting time duration and provides separate statistics for success and error completion.
 * </p>
 */
final class DefaultTimedMetric extends BaseTimedMetric implements TimedMetric {

  private static final String noBuckets = "";

  private final MetricName name;

  private final String bucketRange;

  private final ValueCounter successCounter;

  private final ValueCounter errorCounter;

  DefaultTimedMetric(MetricName name) {
    this.name = name;
    this.bucketRange = noBuckets;
    this.successCounter = new ValueCounter(name);
    this.errorCounter = new ValueCounter(name.append("error"));
  }

  DefaultTimedMetric(MetricName name, String bucketRange) {
    this.name = name;
    this.bucketRange = bucketRange;
    this.successCounter = new ValueCounter(name, bucketRange);
    this.errorCounter = new ValueCounter(name.append("error"));
  }

  public String toString() {
    return name.toString();
  }

  @Override
  public boolean isBucket() {
    return !noBuckets.equals(bucketRange);
  }

  @Override
  public String getBucketRange() {
    return bucketRange;
  }

  @Override
  public void clear() {
    successCounter.reset();
    errorCounter.reset();
  }

  protected static long getTickNanos() {
    return System.nanoTime();
  }

  @Override
  public void collect(MetricStatisticsVisitor collector) {

    TimedStatistics errStats = errorCounter.collectStatistics();
    if (errStats != null) {
      collector.visit(errStats);
    }
    TimedStatistics successStats = successCounter.collectStatistics();
    if (successStats != null) {
      collector.visit(successStats);
    }
  }

  public MetricName getName() {
    return name;
  }

  /**
   * Start an event.
   * <p>
   * The {@link TimedEvent#endWithSuccess()} or {@link TimedEvent#endWithError()} are called at the
   * completion of the timed event.
   * </p>
   */
  @Override
  public TimedEvent startEvent() {
    return new DefaultTimedMetricEvent(this);
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

  /**
   * Add an event duration with opCode indicating success or failure. This is intended for use by
   * enhanced code and not general use.
   */
  @Override
  public void operationEnd(int opCode, long startNanos, boolean activeThreadContext) {
    addEventSince(opCode != 191, startNanos);
    if (activeThreadContext) {
      NestedContext.pop();
    }
  }

  @Override
  public void operationEnd(int opCode, long startNanos) {
    addEventSince(opCode != 191, startNanos);
  }

}
