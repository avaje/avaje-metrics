package io.avaje.metrics.stats;

import io.avaje.metrics.Counter;
import io.avaje.metrics.Metric;

/**
 * Snapshot of the current statistics for a Counter or TimeCounter.
 */
public final class CounterStats implements Counter.Stats {

  final String name;
  final long count;

  /**
   * Construct for Counter which doesn't collect time or high watermark.
   */
  public CounterStats(String name, long count) {
    this.name = name;
    this.count = count;
  }

  @Override
  public void visit(Metric.Visitor visitor) {
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
