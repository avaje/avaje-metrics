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
  
  /**
   * Return the current statistics reseting the internal values if reset is true.
   */
  public ValueStatistics getStatistics(boolean reset) {
    
    if (reset) {
      long now = System.currentTimeMillis();
      return new ValueStatistics(startTime.getAndSet(now), count.sumThenReset(), total.sumThenReset(), max.getAndSet(0));
      
    } else {
      return new ValueStatistics(startTime.get(), count.sum(), total.sum(), max.get());
    }
  }
  
  public void reset() {
    startTime.set(System.currentTimeMillis());
    max.set(0);
    count.reset();
    total.reset();
  }
  
  public long getStartTime() {
    return startTime.get();
  }
  
  public long getCount() {
    return count.sum();
  }
  
  public long getTotal() {
    return total.sum();
  }

  public long getMax() {
    return max.get();
  }
  
}
