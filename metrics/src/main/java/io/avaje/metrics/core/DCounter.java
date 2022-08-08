package io.avaje.metrics.core;

import io.avaje.metrics.Counter;
import io.avaje.metrics.MetricStatsVisitor;

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
   * </p>
   */
  DCounter(String name) {
    super(name);
  }

  /**
   * Clear the collected statistics.
   */
  @Override
  public void reset() {
    count.reset();
  }

  @Override
  public void collect(MetricStatsVisitor collector) {
    final long sum = count.sumThenReset();
    if (sum != 0) {
      final String name = reportName != null ? reportName : reportName(collector);
      collector.visit(new DCounter.DStats(name, sum));
    }
  }

  @Override
  public long count() {
    return count.sum();
  }

  /**
   * Return the name of the metric.
   */
  @Override
  public String name() {
    return name;
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

  /**
   * Snapshot of the current statistics for a Counter or TimeCounter.
   */
  static final class DStats implements Stats {

    final String name;
    final long count;

    /**
     * Construct for Counter which doesn't collect time or high water mark.
     */
    DStats(String name, long count) {
      this.name = name;
      this.count = count;
    }

    @Override
    public void visit(MetricStatsVisitor visitor) {
      visitor.visit(this);
    }

    @Override
    public String toString() {
      return "count:" + count;
    }

    @Override
    public String name() {
      return name;
    }

    /**
     * Return the count of values collected.
     */
    @Override
    public long count() {
      return count;
    }
  }
}
