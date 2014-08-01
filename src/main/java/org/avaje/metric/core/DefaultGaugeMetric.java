package org.avaje.metric.core;

import org.avaje.metric.GaugeDouble;
import org.avaje.metric.GaugeDoubleMetric;
import org.avaje.metric.MetricName;
import org.avaje.metric.MetricVisitor;



/**
 * A Metric that gets its value from a GaugeDouble.
 * <p>
 * GaugeDoubleMetric can be put into groups via {@link DefaultGaugeMetricGroup}.
 * </p>
 */
public class DefaultGaugeMetric implements GaugeDoubleMetric {

  protected final MetricName name;

  protected final GaugeDouble gauge;

  /**
   * Create where the Gauge is a monotonically increasing value.
   * <p>
   * This will determine the delta increase in underlying value and return that
   * for the value.
   * </p>
   */
  public static DefaultGaugeMetric incrementing(MetricName name, GaugeDouble gauge) {
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
  public DefaultGaugeMetric(MetricName name, GaugeDouble gauge) {
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
  public boolean collectStatistics() {
    // There is no 'reset' of startTime required here
    return gauge.getValue() != 0;
  }

  @Override
  public void visit(MetricVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void clearStatistics() {
    // No need to do anything - direct to gauge
  }

  /**
   * Supports monotonically increasing gauges.
   */
  private static class Incrementing extends DefaultGaugeMetric {

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
