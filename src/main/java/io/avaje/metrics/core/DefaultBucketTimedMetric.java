package io.avaje.metrics.core;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.TimedEvent;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.statistics.MetricStatisticsVisitor;

/**
 * Default implementation of BucketTimedMetric.
 */
class DefaultBucketTimedMetric extends BaseTimedMetric implements TimedMetric {

  private static final int OPCODE_ATHROW = 191;

  private final MetricName metricName;

  private final int[] bucketRanges;

  private final TimedMetric[] buckets;

  private final int lastBucketIndex;

  DefaultBucketTimedMetric(MetricName metricName, int[] bucketRanges, TimedMetric[] buckets) {
    this.metricName = metricName;
    this.bucketRanges = bucketRanges;
    this.buckets = buckets;
    this.lastBucketIndex = bucketRanges.length;
  }

  public String toString() {
    return metricName.toString();
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
  public TimedEvent startEvent() {
    return new DefaultTimedMetricEvent(this);
  }

  /**
   * Add the event into the appropriate bucket.
   */
  @Override
  public void addEventDuration(boolean success, long durationNanos) {

    if (!success) {
      // always add errors to the first bucket
      buckets[0].addEventDuration(false, durationNanos);
    } else {
      // convert to millis to find which bucket the event goes into
      long durationMillis = durationNanos / 1000000L;
      for (int i = 0; i < lastBucketIndex; i++) {
        if (durationMillis < bucketRanges[i]) {
          // found the bucket to put the event into
          buckets[i].addEventDuration(true, durationNanos);
          return;
        }
      }
      // add it to the last bucket
      buckets[lastBucketIndex].addEventDuration(true, durationNanos);
    }
  }

  @Override
  public void addEventSince(boolean success, long startNanos) {
    long durationNanos = System.nanoTime() - startNanos;
    addEventDuration(success, durationNanos);
  }

  @Override
  public void operationEnd(int opCode, long startNanos, boolean activeThreadContext) {
    addEventSince(opCode != OPCODE_ATHROW, startNanos);
    if (activeThreadContext) {
      NestedContext.pop();
    }
  }

  @Override
  public void operationEnd(int opCode, long startNanos) {
    addEventSince(opCode != OPCODE_ATHROW, startNanos);
  }

  @Override
  public MetricName getName() {
    return metricName;
  }

  @Override
  public void collect(MetricStatisticsVisitor collector) {
    for (TimedMetric bucket : buckets) {
      bucket.collect(collector);
    }
  }

  @Override
  public void clear() {
    for (int i = 0; i < buckets.length; i++) {
      buckets[i].clear();
    }
  }

  protected static final class DefaultTimedMetricEvent implements TimedEvent {

    private final DefaultBucketTimedMetric metric;

    private final long startNanos;

    /**
     * Create a TimedMetricEvent.
     */
    protected DefaultTimedMetricEvent(DefaultBucketTimedMetric metric) {
      this.metric = metric;
      this.startNanos = System.nanoTime();
    }

    public String toString() {
      return metric.toString() + " durationMillis:" + getDuration();
    }

    /**
     * End specifying whether the event was successful or in error.
     */
    @Override
    public void end(boolean withSuccess) {
      metric.addEventDuration(withSuccess, getDuration());
    }

    /**
     * This timed event ended with successful execution (e.g. Successful SOAP
     * Operation or SQL execution).
     */
    @Override
    public void endWithSuccess() {
      end(true);
    }

    /**
     * This timed event ended with an error or fault execution (e.g. SOAP Fault or
     * SQL exception).
     */
    @Override
    public void endWithError() {
      end(false);
    }


    /**
     * Return the duration in nanos.
     */
    private long getDuration() {
      return System.nanoTime() - startNanos;
    }

  }
}
