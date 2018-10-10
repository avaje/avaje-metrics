package org.avaje.metric.core;

import org.avaje.metric.GaugeDouble;
import org.avaje.metric.GaugeDoubleMetric;
import org.avaje.metric.MetricName;
import org.avaje.metric.statistics.MetricStatisticsVisitor;


/**
 * A Metric that gets its value from a GaugeDouble.
 */
class DefaultGaugeDoubleMetric implements GaugeDoubleMetric {

  protected final MetricName name;

  protected final GaugeDouble gauge;

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

  /**
   * Create a GaugeMetric.
   *
   * @param name
   *          the name of the metric.
   * @param gauge
   *          the gauge used to get the value.
   */
  DefaultGaugeDoubleMetric(MetricName name, GaugeDouble gauge) {
    this.name = name;
    this.gauge = gauge;
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
    double value = gauge.getValue();
    boolean collect = (Double.compare(value, 0.0d) != 0) && (Double.compare(value, lastReported) != 0);
    if (collect) {
      lastReported = value;
      collector.visit(new DGaugeDoubleStatistic(name, value));
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
