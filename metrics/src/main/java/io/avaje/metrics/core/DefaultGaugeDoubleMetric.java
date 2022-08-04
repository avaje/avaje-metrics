package io.avaje.metrics.core;

import io.avaje.metrics.GaugeDouble;
import io.avaje.metrics.GaugeDoubleMetric;
import io.avaje.metrics.MetricName;
import io.avaje.metrics.MetricStatsVisitor;


/**
 * A Metric that gets its value from a GaugeDouble.
 */
class DefaultGaugeDoubleMetric implements GaugeDoubleMetric {

  protected final MetricName name;
  protected final GaugeDouble gauge;
  protected final boolean reportChangesOnly;
  private double lastReported;

  /**
   * Create where the Gauge is a monotonically increasing value.
   * <p>
   * This will determine the delta increase in underlying value and return that
   * for the value.
   * </p>
   */
  static DefaultGaugeDoubleMetric incrementing(MetricName name, GaugeDouble gauge) {
    return new Incrementing(name, gauge);
  }

  DefaultGaugeDoubleMetric(MetricName name, GaugeDouble gauge) {
    this(name, gauge, true);
  }

  DefaultGaugeDoubleMetric(MetricName name, GaugeDouble gauge, boolean reportChangesOnly) {
    this.name = name;
    this.gauge = gauge;
    this.reportChangesOnly = reportChangesOnly;
  }

  @Override
  public MetricName name() {
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
    return gauge.value();
  }

  @Override
  public void collect(MetricStatsVisitor collector) {
    if (!reportChangesOnly) {
      collector.visit(new DGaugeDoubleStatistic(name, gauge.value()));
    } else {
      double value = gauge.value();
      boolean collect = (Double.compare(value, 0.0d) != 0) && (Double.compare(value, lastReported) != 0);
      if (collect) {
        lastReported = value;
        collector.visit(new DGaugeDoubleStatistic(name, value));
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
  private static final class Incrementing extends DefaultGaugeDoubleMetric {

    private double runningValue;

    Incrementing(MetricName name, GaugeDouble gauge) {
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
