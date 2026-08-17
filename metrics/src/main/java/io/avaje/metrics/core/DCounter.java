package io.avaje.metrics.core;

import io.avaje.metrics.CollectionMode;
import io.avaje.metrics.Counter;
import io.avaje.metrics.stats.CounterStats;

import java.util.concurrent.atomic.LongAdder;

/**
 * Count events that occur.
 * <p>
 * For example, this is used to count the error events and warning events logged
 * via log4j or logback.
 */
final class DCounter extends BaseReportName implements Counter {

  private final LongAdder cumulativeCount = new LongAdder();
  private final LongAdder deltaCount = new LongAdder();

  DCounter(ID id, String unit) {
    super(id, unit);
  }

  @Override
  public String toString() {
    return id + ":" + cumulativeCount;
  }

  /**
   * Clear the collected statistics.
   */
  @Override
  public void reset() {
    cumulativeCount.reset();
    deltaCount.reset();
  }

  @Override
  public void collect(Visitor collector) {
    final long sum = (collector.collectionMode() == CollectionMode.CUMULATIVE)
      ? cumulativeCount.sum()
      : deltaCount.sumThenReset();
    if (sum != 0) {
      final ID reportId = reportId(collector);
      collector.visit(new CounterStats(reportId, unit, sum));
    }
  }

  @Override
  public long count() {
    return deltaCount.sum();
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
  public String unit() {
    return unit;
  }

  /**
   * Mark that 1 event has occurred.
   */
  @Override
  public void inc() {
    cumulativeCount.increment();
    deltaCount.increment();
  }

  /**
   * Mark that numberOfEventsOccurred events have occurred.
   */
  @Override
  public void inc(long numberOfEventsOccurred) {
    cumulativeCount.add(numberOfEventsOccurred);
    deltaCount.add(numberOfEventsOccurred);
  }

  @Override
  public void dec() {
    cumulativeCount.decrement();
    deltaCount.decrement();
  }

  @Override
  public void dec(long value) {
    cumulativeCount.add(-value);
    deltaCount.add(-value);
  }

}
