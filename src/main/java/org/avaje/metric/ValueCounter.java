package org.avaje.metric;

import java.util.concurrent.atomic.AtomicLong;

import org.avaje.metric.jsr166e.LongAdder;

/**
 * Used to collect timed execution statistics.
 * <p>
 * It is intended for high concurrent updates to the statistics and relatively infrequent reads.
 * </p>
 */
public class ValueCounter {

  protected final LongAdder count = new LongAdder();

  protected final LongAdder total = new LongAdder();
  
  protected final AtomicLong max = new AtomicLong();  

  protected final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

  protected final boolean collectMax;
  
  public ValueCounter(boolean collectMax) {
    this.collectMax = collectMax;
  }
  
  /**
   * Add a value. Usually the value is Time or Bytes etc.
   */
  public void add(long value) {
     
    count.increment();
    total.add(value);
    if (collectMax && value > max.get()) {
      max.set(value);
    }
  }
  
  public boolean isEmpty() {
    return count.sum() == 0;
  }
  
  public ValueStatistics collectStatistics() {
    boolean empty = isEmpty();
    if (empty) {
      startTime.set(System.currentTimeMillis());
      return null;
    } else {
      return getStatistics(true);
    }
  }

  /**
   * Return the current statistics reseting the internal values if reset is true.
   */
  public ValueStatistics getStatistics(boolean reset) {
    
    if (reset) {
      // Note these values are not guaranteed to be consistent wrt each other
      // but should be reasonably consistent (small time between count and total)
      final long startTimeVal = startTime.getAndSet(System.currentTimeMillis());
      final long countVal = count.sumThenReset();
      final long totalVal = total.sumThenReset();
      final long maxVal = max.getAndSet(0);
      return new ValueStatistics(startTimeVal, countVal, totalVal, maxVal);
      
    } else {
      return new ValueStatistics(startTime.get(), count.sum(), total.sum(), max.get());
    }
  }

  /**
   * Reset just the start time.
   */
  public void resetStartTime() {
    startTime.set(System.currentTimeMillis());
  }
  
  /**
   * Reset all the internal counters and start time.
   */
  public void reset() {
    startTime.set(System.currentTimeMillis());
    max.set(0);
    count.reset();
    total.reset();
  }
  
  /**
   * Return the start time.
   */
  public long getStartTime() {
    return startTime.get();
  }
  
  /**
   * Return the count of values.
   */
  public long getCount() {
    return count.sum();
  }
  
  /**
   * Return the total of values.
   */
  public long getTotal() {
    return total.sum();
  }

  /**
   * Return the max value.
   */
  public long getMax() {
    return max.get();
  }
  
}
