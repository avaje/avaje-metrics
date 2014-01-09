package org.avaje.metric;

/**
 * Snapshot of the current statistics for a Counter or TimeCounter.
 */
public class CounterStatistics {

  protected final long startTime;
  
  protected final long count;

  /**
   * Construct for Counter which doesn't collect time or high water mark.
   */
  public CounterStatistics(long collectionStart, long count) {
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
