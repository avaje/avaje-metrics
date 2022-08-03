package io.avaje.metrics.core;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.statistics.CounterStatistics;

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
  private final MetricName name;

  Counter(MetricName name) {
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
  CounterStatistics collectStatistics() {
    boolean empty = isEmpty();
    if (empty) {
      return null;
    } else {
      return getStatistics();
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
  private CounterStatistics getStatistics() {
    long now = System.currentTimeMillis();
    return new DefaultCounterStatistics(name, count.sumThenReset());
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
  public long getCount() {
    return count.sum();
  }

}
