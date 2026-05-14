package io.avaje.metrics.core;

import io.avaje.metrics.Timer;
import io.avaje.metrics.spi.SpiSpan;
import io.avaje.metrics.spi.SpiTimedSpanFactory.Prepared;
import io.avaje.metrics.spi.SpiTimedSpanFactory;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Designed to capture the duration of timed events.
 * <p>
 * The major difference compared with ValueMetric is that it is specifically oriented towards
 * collecting time duration and provides separate statistics for success and error completion.
 */
final class DTimer implements Timer, TraceableTimer {

  private final ID id;
  private final @Nullable String bucketRange;
  private final ValueCounter successCounter;
  private final ValueCounter errorCounter;
  private final @Nullable Prepared preparedSpan;

  DTimer(ID id) {
    this(id, null, new ValueCounter(id), new ValueCounter(id.suffix(".error")), null);
  }

  DTimer(ID id, String bucketRange) {
    this(id, bucketRange, new ValueCounter(id, bucketRange), new ValueCounter(id.suffix(".error")), null);
  }

  private DTimer(ID id, @Nullable String bucketRange, ValueCounter successCounter,
                 ValueCounter errorCounter, @Nullable Prepared preparedSpan) {
    this.id = id;
    this.bucketRange = bucketRange;
    this.successCounter = successCounter;
    this.errorCounter = errorCounter;
    this.preparedSpan = preparedSpan;
  }

  @Override
  public String toString() {
    return id + ":" + successCounter + ((errorCounter.count() == 0) ? "" : " error:" + errorCounter);
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

  @Override
  public ID id() {
    return id;
  }

  public String name() {
    return id.name();
  }

  @Override
  public void time(Runnable event) {
    if (preparedSpan != null) {
      Event timedEvent = startEvent();
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
      Event timedEvent = startEvent();
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

  /**
   * Start an event.
   * <p>
   * The {@link Event#end()} or {@link Event#endWithError()} are called at the
   * completion of the timed event.
   */
  @Override
  public Event startEvent() {
    return new DTimerEvent(this, startSpan());
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

  @Override
  public Timer withTracing(@Nullable SpiTimedSpanFactory timedSpanFactory) {
    if (this.preparedSpan != null || timedSpanFactory == null) {
      return this;
    }
    var prepared = timedSpanFactory.prepare(id, bucketRange);
    if (prepared == null) {
      return this;
    }
    return new DTimer(id, bucketRange, successCounter, errorCounter, prepared);
  }

  private @Nullable SpiSpan startSpan() {
    return preparedSpan == null ? null : preparedSpan.start();
  }
}
