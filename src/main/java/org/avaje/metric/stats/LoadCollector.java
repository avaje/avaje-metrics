package org.avaje.metric.stats;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.avaje.metric.Clock;
import org.avaje.metric.Stats;

/**
 * Maintains moving averages of loads.
 */
public class LoadCollector {

  private final CollectMovingAverages eventRate;
  private final CollectMovingAverages loadRate;
  private final AtomicLong totalCount = new AtomicLong();
  private final AtomicLong totalLoad = new AtomicLong();

  public LoadCollector(TimeUnit rateUnit, Clock clock, String eventDesc, String workUnits) {
    this.eventRate = new CollectMovingAverages(eventDesc, rateUnit, clock);
    this.loadRate = new CollectMovingAverages(workUnits, rateUnit, clock);
  }

  public void clear() {
    eventRate.clear();
    loadRate.clear();
    totalCount.set(0);
    totalLoad.set(0);
  }

  public void update(long additionalEventCount, long additionalLoad) {

    totalCount.addAndGet(additionalEventCount);
    totalLoad.addAndGet(additionalLoad);
    eventRate.updateAndTick(additionalEventCount);
    loadRate.updateAndTick(additionalLoad);
  }

  public String toString() {
    return "totalCount:" + getTotalCount() + " totalLoad:" + getTotalLoad() + " events.m1: "
        + eventRate.getOneMinuteDisplay() + " load.1min: " + loadRate.getOneMinuteDisplay()
        + " load.10sec: " + loadRate.getTenSecondRateDisplay();
  }

  public long getTotalCount() {
    return totalCount.get();
  }

  public long getTotalLoad() {
    return totalLoad.get();
  }

  public Stats.MovingAverages getEventMovingAverage() {
    return eventRate;
  }

  public Stats.MovingAverages getLoadMovingAverage() {
    return loadRate;
  }

}
