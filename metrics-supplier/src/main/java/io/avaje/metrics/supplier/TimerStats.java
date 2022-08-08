package io.avaje.metrics.supplier;

import io.avaje.metrics.MetricStatsVisitor;
import io.avaje.metrics.Timer;

public final class TimerStats implements Timer.Stats {

  final String name;
  final long count;
  final long total;
  final long max;
  final long mean;

  public TimerStats(String name, long count, long total, long max, long mean) {
    this.name = name;
    this.count = count;
    this.total = total;
    this.max = max;
    this.mean = mean;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public long count() {
    return count;
  }

  @Override
  public long total() {
    return total;
  }

  @Override
  public long max() {
    return max;
  }

  @Override
  public long mean() {
    return mean;
  }

  @Override
  public void visit(MetricStatsVisitor reporter) {
    reporter.visit(this);
  }

  @Override
  public String bucketRange() {
    return null;
  }

}
