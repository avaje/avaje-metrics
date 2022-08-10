package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.MetricStatsVisitor;

import java.util.function.LongSupplier;

abstract class DGaugeLong extends BaseReportName implements GaugeLong {

  static DGaugeLong of(String name, LongSupplier supplier) {
    return new All(name, supplier);
  }

  static DGaugeLong of(String name, LongSupplier supplier, boolean changesOnly) {
    return changesOnly ? new ChangesOnly(name, supplier) : new All(name, supplier);
  }

  static DGaugeLong once(String name, LongSupplier supplier) {
    return new Once(name, supplier);
  }

  protected final LongSupplier supplier;

  protected DGaugeLong(String name, LongSupplier supplier) {
    super(name);
    this.supplier = supplier;
  }

  @Override
  public final String name() {
    return name;
  }

  @Override
  public final String toString() {
    return name;
  }

  /**
   * Return the value.
   */
  @Override
  public final long value() {
    return supplier.getAsLong();
  }

  @Override
  public final void reset() {
    // No need to do anything
  }

  static final class All extends DGaugeLong {

    All(String name, LongSupplier supplier) {
      super(name, supplier);
    }

    @Override
    public void collect(MetricStatsVisitor collector) {
      final String name = reportName != null ? reportName : reportName(collector);
      collector.visit(new DGaugeLongStats(name, supplier.getAsLong()));
    }
  }

  static final class ChangesOnly extends DGaugeLong {
    private long lastReported;

    ChangesOnly(String name, LongSupplier supplier) {
      super(name, supplier);
    }

    @Override
    public void collect(MetricStatsVisitor collector) {
      long value = supplier.getAsLong();
      boolean collect = (value != 0 && value != lastReported);
      if (collect) {
        lastReported = value;
        final String name = reportName != null ? reportName : reportName(collector);
        collector.visit(new DGaugeLongStats(name, value));
      }
    }
  }

  static final class Once extends DGaugeLong {

    Once(String name, LongSupplier supplier) {
      super(name, supplier);
    }

    @Override
    public void collect(MetricStatsVisitor collector) {
      if (reportName == null) {
        String name = reportName(collector);
        collector.visit(new DGaugeLongStats(name, supplier.getAsLong()));
      }
    }
  }
}
