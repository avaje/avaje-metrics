package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricStatsVisitor;
import io.avaje.metrics.Meter;


/**
 * Measure events that occur with a long value. This long value could be bytes
 * or rows processed or time. Typically, you would use Timer for time based
 * events though.
 */
final class DMeter implements Metric, Meter {

  private final String name;
  private final ValueCounter values;

  /**
   * Create with a name.
   */
  DMeter(String name) {
    this.name = name;
    this.values = new ValueCounter(name);
  }

  @Override
  public void collect(MetricStatsVisitor collector) {
    Stats stats = values.collect();
    if (stats != null) {
      collector.visit(stats);
    }
  }

  @Override
  public void reset() {
    values.reset();
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void addEvent(long value) {
    values.add(value);
  }

  @Override
  public long count() {
    return values.count();
  }

  @Override
  public long total() {
    return values.total();
  }

  @Override
  public long max() {
    return values.max();
  }

  @Override
  public long mean() {
    return values.mean();
  }
}
