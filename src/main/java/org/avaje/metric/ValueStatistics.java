package org.avaje.metric;

/**
 * Snapshot of the current statistics for a Counter or TimeCounter.
 */
public class ValueStatistics {

  protected final long startTime;
  
  protected final long count;
  
  protected final long total;

  protected final long max;

  /**
   * Construct for Counter which doesn't collect time or high water mark.
   */
  public ValueStatistics(long collectionStart, long count) {
    this.startTime = collectionStart;
    this.count = count;
    this.total = 0;
    this.max = 0;
  }
  
  /**
   * Construct for TimeCounter.
   */
  public ValueStatistics(long collectionStart, long count, long total, long max) {
    this.startTime = collectionStart;
    this.count = count;
    this.total = total;
    this.max = max;
  }

  public String toString() {
    return "count:"+count+" total:"+total+" max:"+max;
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

  /**
   * Return the total of all the values.
   */
  public long getTotal() {
    return total;
  }

  /**
   * Return the Max value collected.
   */
  public long getMax() {
    return max;
  }

}
