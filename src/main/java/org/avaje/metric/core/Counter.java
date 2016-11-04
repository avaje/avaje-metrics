package org.avaje.metric.core;

import java.util.concurrent.atomic.AtomicLong;

import org.avaje.metric.CounterStatistics;
import java.util.concurrent.atomic.LongAdder;

/**
 * A statistics collection Counter based on LongAdder.
 * <p>
 * It is intended for high (potentially concurrent) updates and low read use
 * cases.
 * </p>
 */
public class Counter {

  protected final LongAdder count = new LongAdder();

  protected final AtomicLong startTime;

  public Counter() {
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
  public CounterStatistics collectStatistics() {
    boolean empty = isEmpty();
    if (empty) {
      startTime.set(System.currentTimeMillis());
      return null;
    } else {
      return getStatistics(true);
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
   * Return the current statistics reseting the internal values if reset is
   * true.
   */
  public CounterStatistics getStatistics(boolean reset) {

    if (reset) {
      long now = System.currentTimeMillis();
      return new DefaultCounterStatistics(startTime.getAndSet(now), count.sumThenReset());

    } else {
      return new DefaultCounterStatistics(startTime.get(), count.sum());
    }
  }

  /**
   * Reset the counter.
   */
  public void reset() {
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
