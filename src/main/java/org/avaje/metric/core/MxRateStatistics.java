package org.avaje.metric.core;

import java.util.concurrent.TimeUnit;

import org.avaje.metric.Stats;

class MxRateStatistics implements RateStatisticsMXBean {

  private final Stats.MovingAverages rateStatistics;

  MxRateStatistics(Stats.MovingAverages rateStatistics) {
    this.rateStatistics = rateStatistics;
  }

  @Override
  public TimeUnit getRateUnit() {
    return rateStatistics.getRateUnit();
  }

  @Override
  public long getCount() {
    return rateStatistics.getCount();
  }

  @Override
  public double getFifteenMinuteRate() {
    return rateStatistics.getFifteenMinuteRate();
  }

  @Override
  public double getFiveMinuteRate() {
    return rateStatistics.getFiveMinuteRate();
  }

  @Override
  public double getOneMinuteRate() {
    return rateStatistics.getOneMinuteRate();
  }

  @Override
  public double getMeanRate() {
    return rateStatistics.getMeanRate();
  }

}
