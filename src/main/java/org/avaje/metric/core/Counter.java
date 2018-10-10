package org.avaje.metric.core;

import org.avaje.metric.MetricName;
import org.avaje.metric.statistics.CounterStatistics;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * A statistics collection Counter based on LongAdder.
 * <p>
 * It is intended for high (potentially concurrent) updates and low read use
 * cases.
 * </p>
 */
class Counter {

  private final LongAdder count = new LongAdder();

  private final AtomicLong startTime;

  private final MetricName name;

  Counter(MetricName name) {
    this.name = name;
    this.startTime = new AtomicLong(System.currentTimeMillis());
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
      startTime.set(System.currentTimeMillis());
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
    return new DefaultCounterStatistics(name, startTime.getAndSet(now), count.sumThenReset());
  }

  /**
   * Reset the counter.
   */
  void reset() {
    startTime.set(System.currentTimeMillis());
    count.reset();
  }

  /**
   * Return the current count.
   */
  public long getCount() {
    return count.sum();
  }

  /**
   * Return the time this counter was last reset.
   */
  public long getStartTime() {
    return startTime.get();
  }
}
