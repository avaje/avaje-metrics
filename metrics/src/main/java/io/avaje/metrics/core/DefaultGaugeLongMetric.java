package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.GaugeLongMetric;
import io.avaje.metrics.MetricName;
import io.avaje.metrics.MetricStatsVisitor;


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
  public long value() {
    return gauge.value();
  }

  @Override
  public void collect(MetricStatsVisitor collector) {
    if (!reportChangesOnly) {
      collector.visit(new DGaugeLongStatistic(name, gauge.value()));
    } else {
      long value = gauge.value();
      boolean collect = (value != 0 && value != lastReported);
      if (collect) {
        lastReported = value;
        collector.visit(new DGaugeLongStatistic(name, value));
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
  static final class Incrementing extends DefaultGaugeLongMetric {

    private long runningValue;

    Incrementing(MetricName name, GaugeLong gauge) {
      super(name, gauge);
    }

    @Override
    public void collect(MetricStatsVisitor collector) {
      long currentValue = super.value();
      if (currentValue > runningValue) {
        collector.visit(new DGaugeLongStatistic(name, this.value()));
      }
    }

    @Override
    public long value() {
      synchronized (this) {
        long nowValue = super.value();
        long diffValue = nowValue - runningValue;
        runningValue = nowValue;
        return diffValue;
      }
    }

  }

}
