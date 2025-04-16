package io.avaje.metrics.core;

import io.avaje.metrics.GaugeDouble;
import io.avaje.metrics.stats.GaugeDoubleStats;

import java.util.function.DoubleSupplier;

/**
 * A Metric that gets its value from a GaugeDouble.
 */
final class DGaugeDouble extends BaseReportName implements GaugeDouble {

  private final DoubleSupplier gauge;

  DGaugeDouble(ID id, DoubleSupplier gauge) {
    super(id);
    this.gauge = gauge;
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
  public String toString() {
    return id + ":" + value();
  }

  /**
   * Return the value.
   */
  @Override
  public double value() {
    return gauge.getAsDouble();
  }

  @Override
  public void collect(Visitor collector) {
    final double value = gauge.getAsDouble();
    if (Double.compare(value, 0.0d) != 0) {
      final ID reportId = reportId(collector);
      collector.visit(new GaugeDoubleStats(reportId, value));
    }
  }

  @Override
  public void reset() {
    // No need to do anything
  }

}
