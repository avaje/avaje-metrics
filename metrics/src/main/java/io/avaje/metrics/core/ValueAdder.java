package io.avaje.metrics.core;

import io.avaje.metrics.CollectionMode;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Accumulates a value while supporting cumulative and delta reads.
 */
final class ValueAdder {

  private final LongAdder value = new LongAdder();
  private final AtomicLong previousValue = new AtomicLong();

  void add(long amount) {
    value.add(amount);
  }

  long get(CollectionMode mode) {
    long currentValue = value.sum();
    if (mode == CollectionMode.CUMULATIVE) {
      return currentValue;
    }

    long previous = previousValue.getAndSet(currentValue);
    return currentValue >= previous ? currentValue - previous : currentValue;
  }

  void reset() {
    value.reset();
    previousValue.set(0);
  }

  long currentValue() {
    return value.sum();
  }

  long deltaValue() {
    long currentValue = value.sum();
    long previous = previousValue.get();
    return currentValue >= previous ? currentValue - previous : currentValue;
  }

  @Override
  public String toString() {
    return Long.toString(currentValue());
  }
}
