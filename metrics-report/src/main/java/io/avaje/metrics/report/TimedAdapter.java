package io.avaje.metrics.report;

import io.avaje.metrics.MetricStatsVisitor;
import io.avaje.metrics.MetricSupplier;
import io.avaje.metrics.Timer;

/**
 * Can be used by {@link MetricSupplier} when adapting metrics from an external source.
 * <p>
 * By default this is a non-bucket timed metric.
 * </p>
 */
public class TimedAdapter implements Timer.Stats {

  private final String name;
  private final String bucketName;
  private final long count;
  private final long total;
  private final long max;

  /**
   * Create with the metric name and values.
   */
  public TimedAdapter(String name, String bucketName, long count, long total, long max) {
    this.name = name;
    this.bucketName = bucketName;
    this.count = count;
    this.total = total;
    this.max = max;
  }

  @Override
  public boolean isBucket() {
    return false;
  }

  @Override
  public String bucketRange() {
    return null;
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
    return (count < 1) ? 0L : Math.round((double) (total / count));
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String nameWithBucket() {
    return bucketName;
  }

  @Override
  public void visit(MetricStatsVisitor visitor) {
    visitor.visit(this);
  }
}
