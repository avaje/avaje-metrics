package org.avaje.metric.stats;

import org.avaje.metric.Stats;
import org.avaje.metric.Stats.Summary;

public class StatsMovingSummary implements Stats.MovingSummary {

  private final Summary oneMin;
  private final Summary fiveMin;
  
  public StatsMovingSummary(Summary oneMin, Summary fiveMin) {
    this.oneMin = oneMin;
    this.fiveMin = fiveMin;
  }
  
  @Override
  public Summary getOneMinuteSummary() {
    return oneMin;
  }

  @Override
  public Summary getFiveMinuteSummary() {
    return fiveMin;
  }

  
}
