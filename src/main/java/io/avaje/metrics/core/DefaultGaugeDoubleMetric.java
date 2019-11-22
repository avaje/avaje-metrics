package io.avaje.metrics.core;

import io.avaje.metrics.GaugeDouble;
import io.avaje.metrics.GaugeDoubleMetric;
import io.avaje.metrics.MetricName;
import io.avaje.metrics.statistics.MetricStatisticsVisitor;


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
  public MetricName getName() {
    return name;
  }

  public String toString() {
    return name + " " + getValue();
  }

  /**
   * Return the value.
   */
  @Override
  public double getValue() {
    return gauge.getValue();
  }

  @Override
  public void collect(MetricStatisticsVisitor collector) {
    if (!reportChangesOnly) {
      collector.visit(new DGaugeDoubleStatistic(name, gauge.getValue()));
    } else {
      double value = gauge.getValue();
      boolean collect = (Double.compare(value, 0.0d) != 0) && (Double.compare(value, lastReported) != 0);
      if (collect) {
        lastReported = value;
        collector.visit(new DGaugeDoubleStatistic(name, value));
      }
    }
  }

  @Override
  public void clear() {
    // No need to do anything - direct to gauge
  }

  /**
   * Supports monotonically increasing gauges.
   */
  private static class Incrementing extends DefaultGaugeDoubleMetric {

    private double runningValue;

    Incrementing(MetricName name, GaugeDouble gauge) {
      super(name, gauge);
    }

    @Override
    public double getValue() {

      synchronized (this) {

        double nowValue = super.getValue();
        double diffValue = nowValue - runningValue;
        runningValue = nowValue;
        return diffValue;
      }
    }

  }

}
