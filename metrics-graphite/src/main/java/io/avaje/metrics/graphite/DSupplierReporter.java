package io.avaje.metrics.graphite;

import io.avaje.metrics.MetricSupplier;

final class DSupplierReporter implements GraphiteSender.Reporter {

  private final MetricSupplier supplier;

  DSupplierReporter(MetricSupplier supplier) {
    this.supplier = supplier;
  }

  @Override
  public void report(GraphiteSender sender) {
    sender.send(supplier.collectMetrics());
  }
}
