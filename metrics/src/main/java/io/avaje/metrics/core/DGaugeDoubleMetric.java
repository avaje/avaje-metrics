package io.avaje.metrics.core;

import io.avaje.metrics.GaugeDoubleMetric;
import io.avaje.metrics.MetricStatsVisitor;

import java.util.function.DoubleSupplier;


/**
 * A Metric that gets its value from a GaugeDouble.
 */
class DGaugeDoubleMetric implements GaugeDoubleMetric {

  protected final String name;
  protected final DoubleSupplier gauge;
  protected final boolean reportChangesOnly;
  private double lastReported;

  /**
   * Create where the Gauge is a monotonically increasing value.
   * <p>
   * This will determine the delta increase in underlying value and return that
   * for the value.
   */
  static DGaugeDoubleMetric incrementing(String name, DoubleSupplier gauge) {
    return new Incrementing(name, gauge);
  }

  DGaugeDoubleMetric(String name, DoubleSupplier gauge) {
    this(name, gauge, true);
  }

  DGaugeDoubleMetric(String name, DoubleSupplier gauge, boolean reportChangesOnly) {
    this.name = name;
    this.gauge = gauge;
    this.reportChangesOnly = reportChangesOnly;
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
  public void collect(MetricStatsVisitor collector) {
    if (!reportChangesOnly) {
      collector.visit(new DGaugeDoubleStats(name, gauge.getAsDouble()));
    } else {
      double value = gauge.getAsDouble();
      boolean collect = (Double.compare(value, 0.0d) != 0) && (Double.compare(value, lastReported) != 0);
      if (collect) {
        lastReported = value;
        collector.visit(new DGaugeDoubleStats(name, value));
      }
    }
  }

  @Override
  public void reset() {
    // No need to do anything - direct to gauge
  }

  /**
   * Supports monotonically increasing gauges.
   */
  private static final class Incrementing extends DGaugeDoubleMetric {

    private double runningValue;

    Incrementing(String name, DoubleSupplier gauge) {
      super(name, gauge);
    }

    @Override
    public double value() {
      synchronized (this) {
        double nowValue = super.value();
        double diffValue = nowValue - runningValue;
        runningValue = nowValue;
        return diffValue;
      }
    }
  }

}
