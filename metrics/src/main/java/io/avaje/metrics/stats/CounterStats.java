package io.avaje.metrics.stats;

import io.avaje.metrics.Counter;
import io.avaje.metrics.Metric;

/**
 * Snapshot of the current statistics for a Counter or TimeCounter.
 */
public final class CounterStats implements Counter.Stats {

  final Metric.ID id;
  final long count;

  /**
   * Construct for Counter which doesn't collect time or high watermark.
   */
  public CounterStats(Metric.ID id, long count) {
    this.id = id;
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
  public Metric.ID id() {
    return id;
  }

  @Override
  public String name() {
    return id.name();
  }

  @Override
  public String[] tags() {
    return id.tags().array();
  }

  /**
   * Return the count of values collected.
   */
  @Override
  public long count() {
    return count;
  }
}
