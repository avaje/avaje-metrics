package io.avaje.metrics.stats;

import io.avaje.metrics.Meter;
import io.avaje.metrics.Metric;

/**
 * Snapshot of the current statistics for a Meter.
 */
public final class MeterStats implements Meter.Stats {

  final Metric.ID id;
  final long count;
  final long total;
  final long max;

  /**
   * Create with all parameters.
   */
  public MeterStats(Metric.ID id, long count, long total, long max) {
    this.id = id;
    this.count = count;
    this.total = total;
    // collection is racy so sanitize the max value if it has not been set
    // this most likely would happen when count = 1 so max = mean
    this.max = max != Long.MIN_VALUE ? max : (count < 1 ? 0 : Math.round((float) total / count));
  }

  @Override
  public String toString() {
    return "count:" + count + " total:" + total + " max:" + max;
  }

  @Override
  public void visit(Metric.Visitor visitor) {
    visitor.visit(this);
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
  public long count() {
    return count;
  }

  @Override
  public long total() {
    return total;
  }

  @Override
  public long max() {
    return max;
  }

  @Override
  public long mean() {
    return (count < 1) ? 0L : Math.round((double) (total / count));
  }
}
