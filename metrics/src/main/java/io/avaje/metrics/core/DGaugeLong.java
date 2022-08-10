package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.MetricStatsVisitor;

import java.util.function.LongSupplier;

class DGaugeLong extends BaseReportName implements GaugeLong {

  protected final LongSupplier supplier;
  protected final boolean reportChangesOnly;
  private long lastReported;

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
    return name;
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
    // No need to do anything
  }

}
