package io.avaje.metrics.core;

import io.avaje.metrics.TimedMetric;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Common implementation for TimedMetric and BucketTimedMetric.
 */
abstract class BaseTimedMetric implements TimedMetric {

  /**
   * Holds a count of the number of requests that we want request level
   * timing collected on.  This is decremented down to 0.
   */
  private final AtomicInteger requestCollection = new AtomicInteger();

  /**
   * Flag set to true when we want to actively turn on 'request' level
   * timing collection.
   */
  private volatile boolean requestTiming;

  /**
   * Return the number of remain requests that we want 'request' level
   * timing collected for.
   */
  @Override
  public int getRequestTiming() {
    return requestCollection.get();
  }

  /**
   * Set the number of requests we want 'request' timing for.
   * <p>
   * If collectionCount is 0 then that effectively turns off
   * request level timing.
   *
   * @param collectionCount the number of requests we want 'request' timing for.
   */
  @Override
  public void setRequestTiming(int collectionCount) {

    // synchronized here ... rare call to set the collectionCount
    // so being safe wrt decrementCollectionCount()

    synchronized (this) {
      requestCollection.set(collectionCount);
      requestTiming = (collectionCount > 0);
    }
  }

  /**
   * Decrement the collectionCount if required.
   * <p>
   * This is called when we have popped/exited the timed metric and are
   * adding it to the list for reporting. This reduces the number of further
   * requests we want to collect request timing for.
   * </p>
   */
  @Override
  public void decrementRequestTiming() {

    // synchronized here but this method is only called when 'per request'
    // timing is actively being collected and the timing information is being
    // popped off and collected

    synchronized (this) {
      // reading and setting requestTiming here ..
      if (requestTiming) {
        // only place where requestCollection is decremented
        int count = requestCollection.decrementAndGet();
        if (count < 1) {
          // only place where requestTiming is set back to false
          requestTiming = false;
        }
      }
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
  public boolean isRequestTiming() {

    // volatile read for requestTiming boolean flag
    if (requestTiming) {
      // explicitly turn on 'request' timing (if it is not already active)
      NestedContext.push(this);
      return true;

    } else {
      // 'request' timing only if there is an already active
      // nested context (thread local)
      return NestedContext.pushIfActive(this);
    }
  }

  @Override
  public Map<String, String> attributes() {
    return null;
  }
}
