package org.avaje.metric;

import java.util.concurrent.atomic.AtomicLong;

import org.avaje.metric.jsr166e.LongAdder;

/**
 * A statistics collection Counter based on LongAdder.
 * <p>
 * It is intended for high (potentially concurrent) updates and low read use cases.
 * </p>
 */
public class Counter {

  protected final LongAdder count = new LongAdder();

  protected final AtomicLong startTime;
  
  public Counter() {
    this.startTime = new AtomicLong(System.currentTimeMillis());
  }
  
  public boolean isEmpty() {
    return count.sum() == 0;
  }
 
  public void add(long eventCount) {     
    count.add(eventCount);
  }
  
  public void increment() {     
    count.increment();
  }

  public void decrement() {     
    count.decrement();
  }

  
  /**
   * Return the current statistics reseting the internal values if reset is true.
   */
  public CounterStatistics getStatistics(boolean reset) {
    
    if (reset) {
      long now = System.currentTimeMillis();
      return new CounterStatistics(startTime.getAndSet(now), count.sumThenReset());
      
    } else {
      return new CounterStatistics(startTime.get(), count.sum());
    }
  }
  
  public void reset() {
    startTime.getAndSet(System.currentTimeMillis());
    count.reset();
  }
  
  public long getCount() {
    return count.sum();
  }
  
  public long getStartTime() {
    return startTime.get();
  }
}
