package io.avaje.metrics.core;

import io.avaje.metrics.Timer;

import java.util.function.Supplier;

/**
 * Default implementation of BucketTimedMetric.
 */
final class DBucketTimer implements Timer {

  private final String metricName;
  private final int[] bucketRanges;
  private final Timer[] buckets;
  private final int lastBucketIndex;

  DBucketTimer(String metricName, int[] bucketRanges, Timer[] buckets) {
    this.metricName = metricName;
    this.bucketRanges = bucketRanges;
    this.buckets = buckets;
    this.lastBucketIndex = bucketRanges.length;
  }

  @Override
  public String toString() {
    return metricName;
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
  public Timer.Event startEvent() {
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
  public void addErr(long startNanos) {
    addEventSince(false, startNanos);
  }

  @Override
  public String name() {
    return metricName;
  }

  @Override
  public void collect(Visitor collector) {
    for (Timer bucket : buckets) {
      bucket.collect(collector);
    }
  }

  @Override
  public void reset() {
    for (Timer bucket : buckets) {
      bucket.reset();
    }
  }

  protected static final class Event implements Timer.Event {

    private final DBucketTimer metric;
    private final long startNanos;

    Event(DBucketTimer metric) {
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
