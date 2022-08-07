package io.avaje.metrics.core;

import java.util.concurrent.atomic.LongAdder;

/**
 * A statistics collection Counter based on LongAdder.
 * <p>
 * It is intended for high (potentially concurrent) updates and low read use
 * cases.
 * </p>
 */
final class Counter {

  private final LongAdder count = new LongAdder();
  private final String name;

  Counter(String name) {
    this.name = name;
  }

  /**
   * Return true if there have been no statistics collected.
   */
  public boolean isEmpty() {
    return count.sum() == 0;
  }

  /**
   * Collect statistics and reset underlying counters in the process. This will
   * return null if no statistics were collected since the last collection.
   */
  io.avaje.metrics.Counter.Stats collect() {
    if (count.sum() == 0) {
      return null;
    } else {
      return stats();
    }
  }

  /**
   * Add a number of events to the counter.
   */
  public void add(long eventCount) {
    count.add(eventCount);
  }

  /**
   * Increment the counter by 1.
   */
  public void increment() {
    count.increment();
  }

  /**
   * Decrement the counter by 1.
   */
  public void decrement() {
    count.decrement();
  }

  /**
   * Return the current statistics reseting the internal values.
   */
  private io.avaje.metrics.Counter.Stats stats() {
    return new DCounterMetric.DStats(name, count.sumThenReset());
  }

  /**
   * Reset the counter.
   */
  void reset() {
    count.reset();
  }

  /**
   * Return the current count.
   */
  public long count() {
    return count.sum();
  }

}
