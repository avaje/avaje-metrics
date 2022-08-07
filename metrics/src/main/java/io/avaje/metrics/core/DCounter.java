package io.avaje.metrics.core;

import io.avaje.metrics.Counter;
import io.avaje.metrics.MetricStatsVisitor;


/**
 * Count events that occur.
 * <p>
 * For example, this is used to count the error events and warning events logged
 * via log4j or logback.
 * </p>
 */
final class DCounter implements Counter {

  private final String name;
  private final io.avaje.metrics.core.Counter counter;

  /**
   * Create the metric with a name and rateUnit.
   * <p>
   * The rateUnit should be chosen to 'scale' the statistics in a reasonable
   * manor - typically events per hour, minute or second.
   * </p>
   */
  DCounter(String name) {
    this.name = name;
    this.counter = new io.avaje.metrics.core.Counter(name);
  }

  /**
   * Clear the collected statistics.
   */
  @Override
  public void reset() {
    counter.reset();
  }

  @Override
  public void collect(MetricStatsVisitor collector) {
    Stats stats = counter.collect();
    if (stats != null) {
      collector.visit(stats);
    }
  }

  @Override
  public long count() {
    return counter.count();
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
    counter.increment();
  }

  /**
   * Mark that numberOfEventsOccurred events have occurred.
   */
  @Override
  public void inc(long numberOfEventsOccurred) {
    counter.add(numberOfEventsOccurred);
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
