package io.avaje.metrics.core.noop;

import io.avaje.metrics.Counter;

final class NoopCounter implements Counter {

  private final String name;

  NoopCounter(String name) {
    this.name = name;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void collect(Visitor visitor) {
    // do nothing
  }

  @Override
  public void reset() {
    // do nothing
  }

  @Override
  public void inc() {
    // do nothing
  }

  @Override
  public void inc(long numberOfEventsOccurred) {
    // do nothing
  }

  @Override
  public void dec() {
    // do nothing
  }

  @Override
  public void dec(long value) {
    // do nothing
  }

  @Override
  public long count() {
    return 0;
  }
}
