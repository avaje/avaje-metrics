package org.avaje.metric.core;

import java.util.concurrent.TimeUnit;

public interface RateStatisticsMXBean {

  public TimeUnit getRateUnit();

  public long getCount();

  public double getFifteenMinuteRate();

  public double getFiveMinuteRate();

  public double getOneMinuteRate();

  public double getMeanRate();

}