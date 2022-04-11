package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.GaugeLongMetric;
import io.avaje.metrics.MetricName;
import io.avaje.metrics.statistics.MetricStatisticsVisitor;


/**
 * A Metric that gets its value from a Gauge.
 */
class DefaultGaugeLongMetric implements GaugeLongMetric {

  protected final MetricName name;
  protected final GaugeLong gauge;
  protected final boolean reportChangesOnly;
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
  static DefaultGaugeLongMetric incrementing(MetricName name, GaugeLong gauge) {
    return new Incrementing(name, gauge);
  }

  /**
   * Create a GaugeMetric.
   *
   * @param name  the name of the metric.
   * @param gauge the gauge used to get the value.
   */
  DefaultGaugeLongMetric(MetricName name, GaugeLong gauge) {
    this(name, gauge, true);
  }

  DefaultGaugeLongMetric(MetricName name, GaugeLong gauge, boolean reportChangesOnly) {
    this.name = name;
    this.gauge = gauge;
    this.reportChangesOnly = reportChangesOnly;
  }

  @Override
  public MetricName getName() {
    return name;
  }

  @Override
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
  public void collect(MetricStatisticsVisitor collector) {
    if (!reportChangesOnly) {
      collector.visit(new DGaugeLongStatistic(name, gauge.getValue()));
    } else {
      long value = gauge.getValue();
      boolean collect = (value != 0 && value != lastReported);
      if (collect) {
        lastReported = value;
        collector.visit(new DGaugeLongStatistic(name, value));
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
  static final class Incrementing extends DefaultGaugeLongMetric {

    private long runningValue;

    Incrementing(MetricName name, GaugeLong gauge) {
      super(name, gauge);
    }

    @Override
    public void collect(MetricStatisticsVisitor collector) {
      long currentValue = super.getValue();
      if (currentValue > runningValue) {
        collector.visit(new DGaugeLongStatistic(name, getValue()));
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
