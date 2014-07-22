package org.avaje.metric.core;

import org.avaje.metric.CounterStatistics;

/**
 * Snapshot of the current statistics for a Counter or TimeCounter.
 */
public class DefaultCounterStatistics implements CounterStatistics {

  protected final long startTime;
  
  protected final long count;

  /**
   * Construct for Counter which doesn't collect time or high water mark.
   */
  public DefaultCounterStatistics(long collectionStart, long count) {
    this.startTime = collectionStart;
    this.count = count;
  }

  public String toString() {
    return "count:"+count;
  }
  
  /**
   * Return the time the counter started statistics collection.
   */
  public long getStartTime() {
    return startTime;
  }

  /**
   * Return the count of values collected.
   */
  public long getCount() {
    return count;
  }
}
