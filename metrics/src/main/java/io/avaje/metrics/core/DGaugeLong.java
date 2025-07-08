package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.stats.GaugeLongStats;

import java.util.function.LongSupplier;

abstract class DGaugeLong extends BaseReportName implements GaugeLong {

  static DGaugeLong of(ID id, LongSupplier supplier) {
    return new All(id, supplier);
  }

  static DGaugeLong of(String name, LongSupplier supplier, boolean changesOnly) {
    return of(ID.of(name), supplier, changesOnly);
  }

  static DGaugeLong of(ID id, LongSupplier supplier, boolean changesOnly) {
    return changesOnly ? new ChangesOnly(id, supplier) : new All(id, supplier);
  }

  static DGaugeLong once(ID id, LongSupplier supplier) {
    return new Once(id, supplier);
  }

  protected final LongSupplier supplier;

  protected DGaugeLong(ID id, LongSupplier supplier) {
    super(id);
    this.supplier = supplier;
  }

  @Override
  public ID id() {
    return id;
  }

  @Override
  public final String name() {
    return id.name();
  }

  @Override
  public final String toString() {
    return id + ":" + supplier.getAsLong();
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

    All(ID id, LongSupplier supplier) {
      super(id, supplier);
    }

    @Override
    public void collect(Visitor collector) {
      final ID reportId = reportId(collector);
      collector.visit(new GaugeLongStats(reportId, supplier.getAsLong()));
    }
  }

  static final class ChangesOnly extends DGaugeLong {
    private long lastReported;

    ChangesOnly(ID id, LongSupplier supplier) {
      super(id, supplier);
    }

    @Override
    public void collect(Visitor collector) {
      long value = supplier.getAsLong();
      boolean collect = (value != 0 && value != lastReported);
      if (collect) {
        lastReported = value;
        final ID reportId = reportId(collector);
        collector.visit(new GaugeLongStats(reportId, value));
      }
    }
  }

  static final class Once extends DGaugeLong {

    Once(ID id, LongSupplier supplier) {
      super(id, supplier);
    }

    @Override
    public void collect(Visitor collector) {
      if (reportId == null) {
        final ID reportId = reportId(collector);
        collector.visit(new GaugeLongStats(reportId, supplier.getAsLong()));
      }
    }
  }
}
