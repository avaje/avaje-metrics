package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import io.avaje.metrics.Meter;

/**
 * Measure events that occur with a long value. This long value could be bytes
 * or rows processed or time. Typically, you would use Timer for time based
 * events though.
 */
final class DMeter implements Metric, Meter {

  private final ID id;
  private final ValueCounter values;

  /**
   * Create with a name.
   */
  DMeter(ID id) {
    this.id = id;
    this.values = new ValueCounter(id);
  }

  @Override
  public String toString() {
    return id + ":" + values;
  }

  @Override
  public void collect(Visitor collector) {
    final Stats stats = values.collect(collector);
    if (stats != null) {
      collector.visit(stats);
    }
  }

  @Override
  public void reset() {
    values.reset();
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
