package io.avaje.metrics.core;

import io.avaje.metrics.Timer;
import io.avaje.metrics.spi.SpiSpan;
import io.avaje.metrics.spi.SpiTimedSpanFactory.Prepared;
import io.avaje.metrics.spi.SpiTimedSpanFactory;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Default implementation of BucketTimedMetric.
 */
final class DBucketTimer implements Timer, TraceableTimer {

  private final ID id;
  final int[] bucketRanges;
  final Timer[] buckets;
  private final int lastBucketIndex;
  private final @Nullable Prepared preparedSpan;

  DBucketTimer(ID id, int[] bucketRanges, Timer[] buckets) {
    this(id, bucketRanges, buckets, null);
  }

  private DBucketTimer(ID id, int[] bucketRanges, Timer[] buckets, @Nullable Prepared preparedSpan) {
    this.id = id;
    this.bucketRanges = bucketRanges;
    this.buckets = buckets;
    this.lastBucketIndex = bucketRanges.length;
    this.preparedSpan = preparedSpan;
  }

  @Override
  public String toString() {
    return id.toString();
  }

  @Override
  public @Nullable String bucketRange() {
    return null;
  }

  @Override
  public void time(Runnable event) {
    if (preparedSpan != null) {
      Timer.Event timedEvent = startEvent();
      try {
        event.run();
        timedEvent.end();
      } catch (RuntimeException | Error e) {
        timedEvent.endWithError(e);
        throw e;
      }
    } else {
      long start = System.nanoTime();
      try {
        event.run();
        add(start);
      } catch (RuntimeException | Error e) {
        addErr(start);
        throw e;
      }
    }
  }

  @Override
  public <T> T time(Supplier<T> event) {
    if (preparedSpan != null) {
      Timer.Event timedEvent = startEvent();
      try {
        final T result = event.get();
        timedEvent.end();
        return result;
      } catch (RuntimeException | Error e) {
        timedEvent.endWithError(e);
        throw e;
      }
    } else {
      long start = System.nanoTime();
      try {
        final T result = event.get();
        add(start);
        return result;
      } catch (RuntimeException | Error e) {
        addErr(start);
        throw e;
      }
    }
  }

  @Override
  public Timer.Event startEvent() {
    return new Event(this, startSpan());
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
  public ID id() {
    return id;
  }

  @Override
  public String name() {
    return id.name();
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

  @Override
  public Timer withTracing(@Nullable SpiTimedSpanFactory timedSpanFactory) {
    if (this.preparedSpan != null || timedSpanFactory == null) {
      return this;
    }
    var prepared = timedSpanFactory.prepare(id, null);
    if (prepared == null) {
      return this;
    }
    return new DBucketTimer(id, bucketRanges, buckets, prepared);
  }

  private @Nullable SpiSpan startSpan() {
    return preparedSpan == null ? null : preparedSpan.start();
  }

  protected static final class Event implements Timer.Event {

    private final DBucketTimer metric;
    private final @Nullable SpiSpan span;
    private final long startNanos;

    Event(DBucketTimer metric, @Nullable SpiSpan span) {
      this.metric = metric;
      this.span = span;
      this.startNanos = System.nanoTime();
    }

    @Override
    public String toString() {
      return metric + " durationMillis:" + duration();
    }

    /**
     * End specifying whether the event was successful or in error.
     */
    @Override
    public void end(boolean withSuccess) {
      end(withSuccess, null);
    }

    private void end(boolean withSuccess, @Nullable Throwable error) {
      metric.addEventDuration(withSuccess, duration());
      if (span != null) {
        if (withSuccess) {
          span.end();
        } else if (error != null) {
          span.endWithError(error);
        } else {
          span.endWithError();
        }
      }
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

    @Override
    public void endWithError(Throwable error) {
      end(false, error);
    }

    /**
     * Return the duration in nanos.
     */
    private long duration() {
      return System.nanoTime() - startNanos;
    }
  }
}
