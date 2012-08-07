package org.avaje.metric.stats;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.avaje.metric.MetricValueEvent;
import org.avaje.metric.Stats;

public class CollectSummary implements Stats.Summary {

  private final AtomicLong min = new AtomicLong();
  private final AtomicLong max = new AtomicLong();
  private final AtomicLong sum = new AtomicLong(); // S
  private final AtomicLong count = new AtomicLong();

  private final long startTime = System.currentTimeMillis();

  /**
   * Clears all recorded values.
   */
  public void clear() {
    count.set(0);
    max.set(Long.MIN_VALUE);
    min.set(Long.MAX_VALUE);
    sum.set(0);
  }

  public long update(List<? extends MetricValueEvent> events) {

    synchronized (this) {

      long total = 0;

      for (int i = 0, max = events.size(); i < max; i++) {
        MetricValueEvent event = events.get(i);
        total += event.getValue();
        update(event.getValue());
      }

      return total;
    }
  }

  public void update(long value) {

    count.incrementAndGet();
    setMax(value);
    setMin(value);
    sum.getAndAdd(value);
  }

  @Override
  public long getSinceSeconds() {
    return (System.currentTimeMillis() - startTime) / 1000;
  }
  
  public double getEventRate(TimeUnit rateUnit) {
    long cnt = getCount();
    if (cnt == 0) {
      return 0d;
    }
    return cnt * 1000d / (System.currentTimeMillis() - startTime) * (double) rateUnit.toSeconds(1);    
  }
  
  @Override
  public long getStartTime() {
    return startTime;
  }

  /**
   * Returns the number of values recorded.
   * 
   * @return the number of values recorded
   */
  public long getCount() {
    return count.get();
  }

  public double getMax() {
    if (getCount() > 0) {
      return max.get();
    }
    return 0.0;
  }

  public double getMin() {
    if (getCount() > 0) {
      return min.get();
    }
    return 0.0;
  }

  public double getMean() {
    if (getCount() > 0) {
      return sum.get() / (double) getCount();
    }
    return 0.0;
  }

  public double getSum() {
    return (double) sum.get();
  }

  private void setMax(long potentialMax) {
    boolean done = false;
    while (!done) {
      final long currentMax = max.get();
      done = currentMax >= potentialMax || max.compareAndSet(currentMax, potentialMax);
    }
  }

  private void setMin(long potentialMin) {
    boolean done = false;
    while (!done) {
      final long currentMin = min.get();
      done = currentMin <= potentialMin || min.compareAndSet(currentMin, potentialMin);
    }
  }

}
