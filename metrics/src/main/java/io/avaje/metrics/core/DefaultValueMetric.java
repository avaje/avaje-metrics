package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricName;
import io.avaje.metrics.ValueMetric;
import io.avaje.metrics.MetricStatsVisitor;


/**
 * Measure events that occur with a long value. This long value could be bytes
 * or rows processed or time. Typically you would use TimedMetric for time based
 * events though.
 */
final class DefaultValueMetric implements Metric, ValueMetric {

  private final MetricName name;
  private final ValueCounter valueCounter;

  /**
   * Create with a name.
   */
  DefaultValueMetric(MetricName name) {
    this.name = name;
    this.valueCounter = new ValueCounter(name);
  }

  @Override
  public void collect(MetricStatsVisitor collector) {
    Stats stats = valueCounter.collectStatistics();
    if (stats != null) {
      collector.visit(stats);
    }
  }

  @Override
  public void reset() {
    valueCounter.reset();
  }

  @Override
  public MetricName name() {
    return name;
  }

  @Override
  public void addEvent(long value) {
    valueCounter.add(value);
  }

  @Override
  public long count() {
    return valueCounter.getCount();
  }

  @Override
  public long total() {
    return valueCounter.getTotal();
  }

  @Override
  public long max() {
    return valueCounter.getMax();
  }

  @Override
  public long mean() {
    return valueCounter.getMean();
  }
}
