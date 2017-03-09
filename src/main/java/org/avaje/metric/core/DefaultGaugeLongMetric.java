package org.avaje.metric.core;

import org.avaje.metric.GaugeLong;
import org.avaje.metric.GaugeLongMetric;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;
import org.avaje.metric.MetricVisitor;

import java.io.IOException;
import java.util.List;


/**
 * A Metric that gets its value from a Gauge.
 */
public class DefaultGaugeLongMetric implements GaugeLongMetric {

  protected final MetricName name;

  protected final GaugeLong gauge;

  /**
   * The last reported value.
   */
  private long lastReported;

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
  public void collectStatistics(List<Metric> list) {
    long value = gauge.getValue();
    boolean collect = (value != 0 && value != lastReported);
    if (collect) {
      lastReported = value;
      list.add(this);
    }
  }

  @Override
  public void visit(MetricVisitor visitor) throws IOException {
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
    public void collectStatistics(List<Metric> list) {
      if (super.getValue() > runningValue) {
        list.add(this);
      }
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
