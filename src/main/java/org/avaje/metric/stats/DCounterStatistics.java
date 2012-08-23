package org.avaje.metric.stats;

import org.avaje.metric.CounterStatistics;

/**
 * Default implementation of CounterStatistics.
 */
public class DCounterStatistics implements CounterStatistics {

  private final long count;
  private final long duration;
  private final long startTime;
  
  public DCounterStatistics() {
    this.count = 0;
    this.startTime = 0; 
    this.duration = 0;
  }
  
  public DCounterStatistics(long count, long durationMillis, long startTime) {
    this.count = count;
    this.startTime = startTime;
    this.duration =  Math.round(durationMillis / 1000d);
  }

  public String toString() {
    return "count:"+count+" dur:"+duration;
  }
  
  @Override
  public long getCount() {
    return count;
  }

  @Override
  public long getDuration() {
    return duration;
  }
  
  @Override
  public long getStartTime() {
    return startTime;
  }

  public DCounterStatistics merge(long extraCount, long extraDuration) {
    
    long totalCount = count + extraCount;
    long totalDuration = duration + extraDuration;
    
    return new DCounterStatistics(totalCount, totalDuration, startTime);
  }
  
}
