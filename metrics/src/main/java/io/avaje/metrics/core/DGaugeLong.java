package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.MetricStatsVisitor;

import java.util.function.LongSupplier;

/**
 * A Metric that gets its value from a Gauge.
 */
class DGaugeLong extends BaseReportName implements GaugeLong {

  protected final LongSupplier supplier;
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
   */
  static DGaugeLong incrementing(String name, LongSupplier gauge) {
    return new Incrementing(name, gauge);
  }

  DGaugeLong(String name, LongSupplier supplier) {
    this(name, supplier, true);
  }

  DGaugeLong(String name, LongSupplier supplier, boolean reportChangesOnly) {
    super(name);
    this.supplier = supplier;
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
  public long value() {
    return supplier.getAsLong();
  }

  @Override
  public void collect(MetricStatsVisitor collector) {
    if (!reportChangesOnly) {
      final String name = reportName != null ? reportName : reportName(collector);
      collector.visit(new DGaugeLongStats(name, supplier.getAsLong()));
    } else {
      long value = supplier.getAsLong();
      boolean collect = (value != 0 && value != lastReported);
      if (collect) {
        lastReported = value;
        final String name = reportName != null ? reportName : reportName(collector);
        collector.visit(new DGaugeLongStats(name, value));
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
  static final class Incrementing extends DGaugeLong {

    private long runningValue;

    Incrementing(String name, LongSupplier gauge) {
      super(name, gauge);
    }

    @Override
    public void collect(MetricStatsVisitor collector) {
      final long currentValue = super.value();
      if (currentValue > runningValue) {
        final String name = reportName != null ? reportName : reportName(collector);
        collector.visit(new DGaugeLongStats(name, this.value()));
      }
    }

    @Override
    public long value() {
      synchronized (this) {
        final long nowValue = super.value();
        final long diffValue = nowValue - runningValue;
        runningValue = nowValue;
        return diffValue;
      }
    }
  }

}
