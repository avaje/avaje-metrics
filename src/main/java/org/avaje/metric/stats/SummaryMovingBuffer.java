package org.avaje.metric.stats;

import org.avaje.metric.Stats;

public class SummaryMovingBuffer {

  private static final StatsSum EMPTY = new StatsSum();
  
  private final Stats.Summary[] buffer;
  
  private int position;
  
  public SummaryMovingBuffer(int size){
    buffer = new Stats.Summary[size];
    position = 0;
  }
  
  public void put(Stats.Summary summary){
    synchronized (this) {
      if (position == buffer.length) {
        position = 0;
      } 
      buffer[position++] = summary;    
    }
  }
  
  public Stats.Summary getLast() {
    synchronized (this) {
      if (position == 0) {
        return buffer[buffer.length-1];        
      } else {
        return buffer[position-1];
      }
    }
  }

  public StatsSum getMovingAggregate() {
    synchronized (this) {
      if (buffer[0] == null) {
        return EMPTY;
      }
      StatsSum sum = new StatsSum(buffer[0]);
      for (int i = 1; i < buffer.length; i++) {
        if (buffer[i] != null){
          sum = sum.merge(buffer[i]);
        }
      }
      return sum;
    }
  }
}
