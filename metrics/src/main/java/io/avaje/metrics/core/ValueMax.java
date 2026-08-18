package io.avaje.metrics.core;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.function.LongSupplier;

/**
 * Accumulates a maximum value and publishes it in rolling 59-second windows.
 */
final class ValueMax {

  private static final long WINDOW_NANOS = TimeUnit.SECONDS.toNanos(59);

  private final LongSupplier nanoTime;
  private final LongAccumulator value = new LongAccumulator(Math::max, 0);
  private long lastResetNanos;
  private volatile long published;

  ValueMax() {
    this(System::nanoTime);
  }

  ValueMax(LongSupplier nanoTime) {
    this.nanoTime = nanoTime;
    this.lastResetNanos = nanoTime.getAsLong() - 2 * WINDOW_NANOS;
  }

  void add(long amount) {
    value.accumulate(amount);
  }

  synchronized long collect() {
    long now = nanoTime.getAsLong();
    if (now - lastResetNanos >= WINDOW_NANOS) {
      published = value.getThenReset();
      lastResetNanos = now;
    }
    return published;
  }

  long current() {
    return value.get();
  }

  synchronized void reset() {
    value.reset();
    published = 0;
    lastResetNanos = nanoTime.getAsLong() - 2 * WINDOW_NANOS;
  }
}
