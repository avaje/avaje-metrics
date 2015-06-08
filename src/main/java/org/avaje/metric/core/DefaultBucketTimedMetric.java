package org.avaje.metric.core;

import org.avaje.metric.BucketTimedMetric;
import org.avaje.metric.MetricName;
import org.avaje.metric.MetricVisitor;
import org.avaje.metric.TimedEvent;
import org.avaje.metric.TimedMetric;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default implementation of BucketTimedMetric.
 */
public class DefaultBucketTimedMetric implements BucketTimedMetric {

  private static final int OPCODE_ATHROW = 191;

  private final MetricName metricName;
  
  private final int[] bucketRanges;
  
  private final TimedMetric[] buckets;
  
  private final int lastBucketIndex;

  private final AtomicInteger requestCollection = new AtomicInteger();

  private boolean requestTiming;

  public DefaultBucketTimedMetric(MetricName metricName, int[] bucketRanges, TimedMetric[] buckets) {
    this.metricName = metricName;
    this.bucketRanges = bucketRanges;
    this.buckets = buckets;
    this.lastBucketIndex = bucketRanges.length;
  }

  public String toString() {
    return metricName.toString();
  }

  @Override
  public int[] getBucketRanges() {
    return bucketRanges;
  }

  @Override
  public TimedMetric[] getBuckets() {
    return buckets;
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
    
    // convert to millis to find which bucket the event goes into
    long durationMillis = durationNanos / 1000000L;
    for (int i = 0; i < lastBucketIndex; i++) {
      if (durationMillis < bucketRanges[i]) {
        // found the bucket to put the event into
        buckets[i].addEventDuration(success, durationNanos);
        return;
      }
    }
    // add it to the last bucket
    buckets[lastBucketIndex].addEventDuration(success, durationNanos);
  }

  @Override
  public void addEventSince(boolean success, long startNanos) {
    long durationNanos = System.nanoTime() - startNanos;
    addEventDuration(success, durationNanos);
  }

  @Override
  public void setRequestTimingCollection(int collectionCount) {

    requestCollection.set(collectionCount);
    requestTiming = collectionCount != 0;
  }

  @Override
  public int getRequestTimingCollection() {
    return requestCollection.get();
  }

  @Override
  public void decrementCollectionCount() {
    int count = requestCollection.decrementAndGet();
    if (count < 1) {
      requestTiming = false;
    }
  }

  /**
   * Return true if this TimedMetric has been pushed onto an active context for this thread.
   * <p>
   * This means that the current thread is actively collecting timing entries and this metric
   * has been pushed onto the nested context.
   * </p>
   */
  @Override
  public boolean isActiveThreadContext() {

    if (requestTiming) {
      // explicitly turned on for this metric
      NestedContext.push(this);
      return true;

    } else {
      // on if there is an active nested context
      return NestedContext.pushIfActive(this);
    }
  }

  @Override
  public void operationEnd(int opCode, long startNanos, boolean activeThreadContext) {
    addEventSince(opCode != OPCODE_ATHROW, startNanos);
    if (activeThreadContext) {
      NestedContext.pop();
    }
  }

  @Override
  public MetricName getName() {
    return metricName;
  }

  @Override
  public boolean collectStatistics() {
    int nonEmptyBuckets = 0;
    for (int i = 0; i < buckets.length; i++) {
      if (buckets[i].collectStatistics()) {
        nonEmptyBuckets++;
      }
    }
    return nonEmptyBuckets > 0;
  }

  @Override
  public void visit(MetricVisitor visitor) throws IOException {
    visitor.visit(this);
  }

  @Override
  public void clearStatistics() {
    for (int i = 0; i < buckets.length; i++) {
      buckets[i].clearStatistics();
    }
  }

  protected final class DefaultTimedMetricEvent implements TimedEvent {

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
