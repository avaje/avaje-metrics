package io.avaje.metrics.stats;

import io.avaje.metrics.Counter;
import io.avaje.metrics.Metric;

import static java.util.Objects.requireNonNull;

/**
 * Snapshot of the current statistics for a Counter.
 */
public final class CounterStats implements Counter.Stats {

  final Metric.ID id;
  final String unit;
  final long count;

  /**
   * Construct for Counter which doesn't collect time or high watermark.
   */
  public CounterStats(Metric.ID id, long count) {
    this(id, "{event}", count);
  }

  /**
   * Construct for Counter with an explicit unit.
   */
  public CounterStats(Metric.ID id, String unit, long count) {
    this.id = id;
    this.unit = normalizeUnit(unit);
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

  @Override
  public String unit() {
    return unit;
  }

  /**
   * Return the count of values collected.
   */
  @Override
  public long count() {
    return count;
  }

  private static String normalizeUnit(String unit) {
    var normalized = requireNonNull(unit, "unit");
    return normalized.isBlank() ? "" : normalized;
  }
}
