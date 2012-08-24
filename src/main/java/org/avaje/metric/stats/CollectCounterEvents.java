package org.avaje.metric.stats;

import java.util.concurrent.atomic.AtomicLong;

import org.avaje.metric.CounterStatistics;

/**
 * Collect counts (AtomicLong) but pair with a duration over which the counts
 * have been collected.
 * <p>
 * Typically this will be collected with reset every 1 minute to report the
 * count of events in the last minute and reset.
 * </p>
 * <p>
 * Keeps a lastCounterStats so that a casual read will give reasonable results
 * by combing the current counts with the lastCounterStats.
 * </p>
 */
public class CollectCounterEvents {

  private final AtomicLong counter = new AtomicLong();

  private DCounterStatistics lastCounterStats = new DCounterStatistics();

  private long startTime = System.currentTimeMillis();

  public void updateAndTick(long eventCount) {
    counter.addAndGet(eventCount);
  }

  public void reset() {
    synchronized (this) {
      counter.set(0);
      startTime = System.currentTimeMillis();
    }
  }

  public boolean isEmpty() {
    return counter.get() == 0;
  }

  public CounterStatistics getCounterStatistics(boolean reset) {
    synchronized (this) {
      if (reset) {
        // return the current results and reset
        long count = counter.getAndSet(0);
        long durMillis = System.currentTimeMillis() - startTime;

        lastCounterStats = new DCounterStatistics(count, durMillis, startTime);
        startTime = System.currentTimeMillis();
        return lastCounterStats;

      } else {
        // return a merge of current count plus the last one collected.
        // That should give a decent result when calculated just after
        // a collect with reset
        long count = counter.get();
        long durMillis = System.currentTimeMillis() - startTime;
        if (lastCounterStats.getDuration() == 0) {
          return new DCounterStatistics(count, durMillis, startTime);
        } else {
          return lastCounterStats.merge(count, durMillis);
        }

      }
    }
  }

}
