package io.avaje.metrics.statistics;

import io.avaje.metrics.MetricSupplier;

/**
 * Can be used by {@link MetricSupplier} when adapting metrics from an external source.
 * <p>
 * By default this is a non-bucket timed metric.
 * </p>
 */
public class TimedAdapter implements TimedStatistics {

  private final String name;
  private final String bucketName;
  private final long startTime;
  private final long count;
  private final long total;
  private final long max;

  /**
   * Create with the metric name and values.
   */
  public TimedAdapter(String name, String bucketName, long startTime, long count, long total, long max) {
    this.name = name;
    this.bucketName = bucketName;
    this.startTime = startTime;
    this.count = count;
    this.total = total;
    this.max = max;
  }

  @Override
  public boolean isBucket() {
    return false;
  }

  @Override
  public String getBucketRange() {
    return null;
  }

  @Override
  public long getStartTime() {
    return startTime;
  }

  @Override
  public long getCount() {
    return count;
  }

  @Override
  public long getTotal() {
    return total;
  }

  @Override
  public long getMax() {
    return max;
  }

  @Override
  public long getMean() {
    return (count < 1) ? 0L : Math.round((double) (total / count));
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getNameWithBucket() {
    return bucketName;
  }

  @Override
  public void visit(MetricStatisticsVisitor visitor) {
    visitor.visit(this);
  }
}
