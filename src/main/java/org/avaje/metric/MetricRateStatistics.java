package org.avaje.metric;

import java.util.concurrent.TimeUnit;

public interface MetricRateStatistics {

  public TimeUnit getRateUnit();

  public long getCount();

  public double getFifteenMinuteRate();

  public double getFiveMinuteRate();

  public double getOneMinuteRate();

  public double getMeanRate();

}