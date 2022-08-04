package io.avaje.metrics.core;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.TimedEvent;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.MetricStatsVisitor;

import java.util.function.Supplier;

/**
 * Default implementation of BucketTimedMetric.
 */
final class DBucketTimedMetric implements TimedMetric {

  private final MetricName metricName;
  private final int[] bucketRanges;
  private final TimedMetric[] buckets;
  private final int lastBucketIndex;

  DBucketTimedMetric(MetricName metricName, int[] bucketRanges, TimedMetric[] buckets) {
    this.metricName = metricName;
    this.bucketRanges = bucketRanges;
    this.buckets = buckets;
    this.lastBucketIndex = bucketRanges.length;
  }

  @Override
  public String toString() {
    return metricName.toString();
  }

  @Override
  public boolean isBucket() {
    return false;
  }

  @Override
  public String bucketRange() {
    return null;
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

  @Override
  public TimedEvent startEvent() {
    return new Event(this);
  }

  /**
   * Add the event into the appropriate bucket.
   */
  @Override
  public void addEventDuration(boolean success, long durationNanos) {
    if (!success) {
      // always add errors to the first bucket
      buckets[0].addEventDuration(false, durationNanos);
    } else {
      // convert to millis to find which bucket the event goes into
      long durationMillis = durationNanos / 1000000L;
      for (int i = 0; i < lastBucketIndex; i++) {
        if (durationMillis < bucketRanges[i]) {
          // found the bucket to put the event into
          buckets[i].addEventDuration(true, durationNanos);
          return;
        }
      }
      // add it to the last bucket
      buckets[lastBucketIndex].addEventDuration(true, durationNanos);
    }
  }

  @Override
  public void addEventSince(boolean success, long startNanos) {
    long durationNanos = System.nanoTime() - startNanos;
    addEventDuration(success, durationNanos);
  }

  @Override
  public void add(long startNanos) {
    addEventSince(true, startNanos);
  }

  @Override
  public void add(long startNanos, boolean activeThreadContext) {
    addEventSince(true, startNanos);
//    if (activeThreadContext) {
//      NestedContext.pop();
//    }
  }

  @Override
  public void addErr(long startNanos) {
    addEventSince(false, startNanos);
  }

  @Override
  public void addErr(long startNanos, boolean activeThreadContext) {
    addEventSince(false, startNanos);
//    if (activeThreadContext) {
//      NestedContext.pop();
//    }
  }

  @Override
  public MetricName name() {
    return metricName;
  }

  @Override
  public void collect(MetricStatsVisitor collector) {
    for (TimedMetric bucket : buckets) {
      bucket.collect(collector);
    }
  }

  @Override
  public void reset() {
    for (TimedMetric bucket : buckets) {
      bucket.reset();
    }
  }

  protected static final class Event implements TimedEvent {

    private final DBucketTimedMetric metric;
    private final long startNanos;

    Event(DBucketTimedMetric metric) {
      this.metric = metric;
      this.startNanos = System.nanoTime();
    }

    @Override
    public String toString() {
      return metric.toString() + " durationMillis:" + duration();
    }

    /**
     * End specifying whether the event was successful or in error.
     */
    @Override
    public void end(boolean withSuccess) {
      metric.addEventDuration(withSuccess, duration());
    }

    /**
     * This timed event ended with successful execution (e.g. Successful SOAP
     * Operation or SQL execution).
     */
    @Override
    public void end() {
      end(true);
    }

    /**
     * This timed event ended with an error or fault execution (e.g. SOAP Fault or
     * SQL exception).
     */
    @Override
    public void endWithError() {
      end(false);
    }


    /**
     * Return the duration in nanos.
     */
    private long duration() {
      return System.nanoTime() - startNanos;
    }

  }
}
