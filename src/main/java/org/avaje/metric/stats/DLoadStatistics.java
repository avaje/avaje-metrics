package org.avaje.metric.stats;

import org.avaje.metric.LoadStatistics;

/**
 * Default implementation of CounterStatistics.
 */
public class DLoadStatistics implements LoadStatistics {

  private final long count;
  private final long load;
  private final long duration;
  private final long startTime;

  
  public DLoadStatistics() {
    this.count = 0;
    this.load = 0;
    this.startTime = 0;
    this.duration = 0;
  }
  
  public DLoadStatistics(long count, long load, long durationMillis, long startTime) {
    this.count = count;
    this.load = load;
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
  
  public long getLoad() {
    return load;
  }

  public long getStartTime() {
    return startTime;
  }

  @Override
  public long getDuration() {
    return duration;
  }
  
  public DLoadStatistics merge(long extraCount, long extraLoad, long durationMillis) {
    
    long totalCount = count + extraCount;
    long totalLoad = load + extraLoad;
   
    return new DLoadStatistics(totalCount, totalLoad, durationMillis, startTime);
  }
  
}
