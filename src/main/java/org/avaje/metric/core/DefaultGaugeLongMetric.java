package org.avaje.metric.core;

import org.avaje.metric.GaugeLong;
import org.avaje.metric.GaugeLongMetric;
import org.avaje.metric.MetricName;
import org.avaje.metric.MetricVisitor;



/**
 * A Metric that gets its value from a Gauge.
 * <p>
 * GaugeMetric can be put into groups via {@link DefaultGaugeDoubleGroup}.
 * </p>
 */
public class DefaultGaugeLongMetric implements GaugeLongMetric {

  protected final MetricName name;

  protected final GaugeLong gauge;

  /**
   * Create where the Gauge is a monotonically increasing value.
   * <p>
   * This will determine the delta increase in underlying value and return that
   * for the value.
   * </p>
   */
  public static DefaultGaugeLongMetric incrementing(MetricName name, GaugeLong gauge) {
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
  public DefaultGaugeLongMetric(MetricName name, GaugeLong gauge) {
    this.name = name;
    this.gauge = gauge;
  }
  
  /**
   * Create as a non whole number and using group, type and name as the metric name.
   */
  public DefaultGaugeLongMetric(String group, String type, String name, GaugeLong gauge) {
    this(new DefaultMetricName(group, type, name), gauge);
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
  public long getValue() {
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
  private static class Incrementing extends DefaultGaugeLongMetric {

    private long runningValue;

    Incrementing(MetricName name, GaugeLong gauge) {
      super(name, gauge);
    }

    @Override
    public long getValue() {

      synchronized (this) {

        long nowValue = super.getValue();
        long diffValue = nowValue - runningValue;
        runningValue = nowValue;
        return diffValue;
      }
    }

  }

}
