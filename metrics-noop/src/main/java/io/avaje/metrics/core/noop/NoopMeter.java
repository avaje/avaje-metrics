package io.avaje.metrics.core.noop;

import io.avaje.metrics.Meter;

class NoopMeter implements Meter {

  private final String metricName;

  NoopMeter(String metricName) {
    this.metricName = metricName;
  }

  @Override
  public String name() {
    return metricName;
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
  public void addEvent(long value) {
    // do nothing
  }

  @Override
  public long count() {
    return 0;
  }

  @Override
  public long total() {
    return 0;
  }

  @Override
  public long max() {
    return 0;
  }

  @Override
  public long mean() {
    return 0;
  }
}
