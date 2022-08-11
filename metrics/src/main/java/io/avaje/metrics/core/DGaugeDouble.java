package io.avaje.metrics.core;

import io.avaje.metrics.GaugeDouble;
import io.avaje.metrics.stats.GaugeDoubleStats;

import java.util.function.DoubleSupplier;

/**
 * A Metric that gets its value from a GaugeDouble.
 */
final class DGaugeDouble extends BaseReportName implements GaugeDouble {

  private final DoubleSupplier gauge;

  DGaugeDouble(String name, DoubleSupplier gauge) {
    super(name);
    this.gauge = gauge;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String toString() {
    return name + " " + value();
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
      final String name = reportName != null ? reportName : reportName(collector);
      collector.visit(new GaugeDoubleStats(name, value));
    }
  }

  @Override
  public void reset() {
    // No need to do anything
  }

}
