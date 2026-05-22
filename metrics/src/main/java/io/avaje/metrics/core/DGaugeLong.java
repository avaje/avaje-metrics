package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.stats.GaugeLongStats;

import java.util.function.LongSupplier;

abstract class DGaugeLong extends BaseReportName implements GaugeLong {

  static DGaugeLong of(ID id, LongSupplier supplier) {
    return of(id, "", supplier);
  }

  static DGaugeLong of(ID id, String unit, LongSupplier supplier) {
    return new All(id, unit, supplier);
  }

  static DGaugeLong of(String name, LongSupplier supplier, boolean changesOnly) {
    return of(ID.of(name), "", supplier, changesOnly);
  }

  static DGaugeLong of(ID id, LongSupplier supplier, boolean changesOnly) {
    return of(id, "", supplier, changesOnly);
  }

  static DGaugeLong of(ID id, String unit, LongSupplier supplier, boolean changesOnly) {
    return changesOnly ? new ChangesOnly(id, unit, supplier) : new All(id, unit, supplier);
  }

  static DGaugeLong once(ID id, LongSupplier supplier) {
    return once(id, "", supplier);
  }

  static DGaugeLong once(ID id, String unit, LongSupplier supplier) {
    return new Once(id, unit, supplier);
  }

  protected final LongSupplier supplier;

  protected DGaugeLong(ID id, String unit, LongSupplier supplier) {
    super(id, unit);
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
  public final String unit() {
    return unit;
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

    All(ID id, String unit, LongSupplier supplier) {
      super(id, unit, supplier);
    }

    @Override
    public void collect(Visitor collector) {
      final ID reportId = reportId(collector);
      collector.visit(new GaugeLongStats(reportId, unit, supplier.getAsLong()));
    }
  }

  static final class ChangesOnly extends DGaugeLong {
    private long lastReported;

    ChangesOnly(ID id, String unit, LongSupplier supplier) {
      super(id, unit, supplier);
    }

    @Override
    public void collect(Visitor collector) {
      long value = supplier.getAsLong();
      boolean collect = (value != 0 && value != lastReported);
      if (collect) {
        lastReported = value;
        final ID reportId = reportId(collector);
        collector.visit(new GaugeLongStats(reportId, unit, value));
      }
    }
  }

  static final class Once extends DGaugeLong {

    Once(ID id, String unit, LongSupplier supplier) {
      super(id, unit, supplier);
    }

    @Override
    public void collect(Visitor collector) {
      if (reportId == null) {
        final ID reportId = reportId(collector);
        collector.visit(new GaugeLongStats(reportId, unit, supplier.getAsLong()));
      }
    }
  }
}
