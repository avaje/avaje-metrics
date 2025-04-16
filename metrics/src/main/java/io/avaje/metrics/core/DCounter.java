package io.avaje.metrics.core;

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

  private final LongAdder count = new LongAdder();

  /**
   * Create the metric with a name and rateUnit.
   * <p>
   * The rateUnit should be chosen to 'scale' the statistics in a reasonable
   * manor - typically events per hour, minute or second.
   */
  DCounter(ID id) {
    super(id);
  }

  @Override
  public String toString() {
    return id + ":" + count;
  }

  /**
   * Clear the collected statistics.
   */
  @Override
  public void reset() {
    count.reset();
  }

  @Override
  public void collect(Visitor collector) {
    final long sum = count.sumThenReset();
    if (sum != 0) {
      final ID reportId = reportId(collector);
      collector.visit(new CounterStats(reportId, sum));
    }
  }

  @Override
  public long count() {
    return count.sum();
  }

  @Override
  public ID id() {
    return id;
  }

  @Override
  public String name() {
    return id.name();
  }

  /**
   * Mark that 1 event has occurred.
   */
  @Override
  public void inc() {
    count.increment();
  }

  /**
   * Mark that numberOfEventsOccurred events have occurred.
   */
  @Override
  public void inc(long numberOfEventsOccurred) {
    count.add(numberOfEventsOccurred);
  }

  @Override
  public void dec() {
    count.decrement();
  }

  @Override
  public void dec(long value) {
    count.add(-value);
  }

}
