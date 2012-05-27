package org.avaje.metric;


public interface Metric {

  public MetricName getName();
  
  public void clearStatistics();
  
  public void updateStatistics();
}
