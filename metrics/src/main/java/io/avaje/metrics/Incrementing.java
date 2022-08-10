package io.avaje.metrics;

import java.util.function.LongSupplier;

final class Incrementing implements LongSupplier {

  private final LongSupplier supplier;
  private long last;

  Incrementing(LongSupplier supplier) {
    this.supplier = supplier;
  }

  @Override
  public long getAsLong() {
    final long current = supplier.getAsLong();
    final long diff = current - last;
    this.last = current;
    return diff;
  }
}
