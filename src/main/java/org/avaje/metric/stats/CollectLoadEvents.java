package org.avaje.metric.stats;

import java.util.concurrent.atomic.AtomicLong;

import org.avaje.metric.LoadStatistics;

/**
 * Maintains moving averages of loads.
 */
public class CollectLoadEvents {

  private final AtomicLong counter = new AtomicLong();
  private final AtomicLong loadCounter = new AtomicLong();
  
  private DLoadStatistics lastStats = new DLoadStatistics();
  
  private long startTime = System.currentTimeMillis();

  public CollectLoadEvents() {
  }

  public void reset() {
    synchronized (this) {
      counter.set(0);
      loadCounter.set(0);
      lastStats = new DLoadStatistics();
      startTime = System.currentTimeMillis();
    }
  }

  public void update(long additionalEventCount, long additionalLoad) {

    counter.addAndGet(additionalEventCount);
    loadCounter.addAndGet(additionalLoad);
  }

  public String toString() {
    return "count:" + getCount() + " load:" + getLoad();
  }
  
  public long getCount() {
    return counter.get();
  }

  public long getLoad() {
    return loadCounter.get();
  }

  public boolean isEmpty() {
    return counter.get() == 0;
  }
  
  public LoadStatistics getLoadStatistics(boolean reset) {
    synchronized (this) {
      
      if (reset) {
        // return the current results and reset
        long count = counter.getAndSet(0);
        long load = loadCounter.getAndSet(0);
        long durMillis = System.currentTimeMillis() - startTime;

        startTime = System.currentTimeMillis();
        lastStats = new DLoadStatistics(count, load, durMillis, startTime);
        return lastStats;

      } else {
        // return a merge of current stats plus the last one collected.
        // That should give a decent result when calculated just after
        // a collect with reset
        long count = counter.get();
        long load = loadCounter.get();
        long durMillis = System.currentTimeMillis() - startTime;
        if (lastStats.getDuration() == 0) {
          return new DLoadStatistics(count, load, durMillis, startTime);
        } else {
          return lastStats.merge(count, load, durMillis);
        }

      }
    }
  }
  
  

}
