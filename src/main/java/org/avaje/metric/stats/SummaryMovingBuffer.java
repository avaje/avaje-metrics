package org.avaje.metric.stats;

import org.avaje.metric.Stats;
import org.avaje.metric.Stats.MovingSummary;

public class SummaryMovingBuffer {

  private static final StatsSum EMPTY = new StatsSum();

  private final Stats.Summary[] buffer;

  private int position;

  public SummaryMovingBuffer(int size) {
    buffer = new Stats.Summary[size];
    position = 0;
  }

  public void put(Stats.Summary summary) {
    synchronized (this) {
      if (position == buffer.length) {
        position = 0;
      }
      buffer[position++] = summary;
    }
  }

  private Stats.Summary getLast() {
   
    if (position == 0) {
      return buffer[buffer.length - 1];
    } else {
      return buffer[position - 1];
    }
  }

  public MovingSummary getMovingSummary(StatsSum current) {
    synchronized (this) {
      
      StatsSum fiveMinSummary = getFullBufferAggregate();
      Stats.Summary oneMinSummary = getLast();
      
      fiveMinSummary = current.merge(fiveMinSummary);
      oneMinSummary = current.merge(oneMinSummary);
      
      return new StatsMovingSummary(oneMinSummary, fiveMinSummary);
    }
  }
  
  protected StatsSum getFiveMinuteSummary() {
    synchronized (this) {
      return getFullBufferAggregate();
    }
  }
  
  private StatsSum getFullBufferAggregate() {
    
      if (buffer[0] == null) {
        return EMPTY;
      }
      StatsSum sum = new StatsSum(buffer[0]);
      for (int i = 1; i < buffer.length; i++) {
        if (buffer[i] != null) {
          sum = sum.merge(buffer[i]);
        }
      }
      return sum;
    }
  
}
