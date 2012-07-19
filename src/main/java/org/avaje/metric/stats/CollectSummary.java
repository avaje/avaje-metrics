package org.avaje.metric.stats;

import static java.lang.Math.sqrt;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.avaje.metric.MetricValueEvent;
import org.avaje.metric.Stats;


public class CollectSummary implements Stats.Summary {

  /**
   * Cache arrays for the variance calculation, so as to avoid memory
   * allocation.
   */
  private static class ArrayCache extends ThreadLocal<double[]> {
    @Override
    protected double[] initialValue() {
      return new double[2];
    }
  }

  private final AtomicLong min = new AtomicLong();
  private final AtomicLong max = new AtomicLong();
  private final AtomicLong sum = new AtomicLong();
  
  // These are for the Welford algorithm for calculating
  // running variance without floating-point doom.
  private final AtomicReference<double[]> variance = new AtomicReference<double[]>(new double[] { -1, 0 }); // M,
                                                                                                            // S
  private final AtomicLong count = new AtomicLong();

  private final ArrayCache arrayCache = new ArrayCache();

  /**
   * Clears all recorded values.
   */
  public void clear() {
      count.set(0);
      max.set(Long.MIN_VALUE);
      min.set(Long.MAX_VALUE);
      sum.set(0);
      variance.set(new double[]{-1, 0});
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
    updateVariance(value);
  }

  
  @Override
  public long getSinceSeconds() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public long getStartTime() {
    // TODO Auto-generated method stub
    return 0;
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

  public double getStdDev() {
    if (getCount() > 0) {
      return sqrt(getVariance());
    }
    return 0.0;
  }

  public double getSum() {
    return (double) sum.get();
  }

  private double getVariance() {
    if (getCount() <= 1) {
      return 0.0;
    }
    return variance.get()[1] / (getCount() - 1);
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

  private void updateVariance(long value) {
    boolean done = false;
    while (!done) {
      final double[] oldValues = variance.get();
      final double[] newValues = arrayCache.get();
      if (oldValues[0] == -1) {
        newValues[0] = value;
        newValues[1] = 0;
      } else {
        final double oldM = oldValues[0];
        final double oldS = oldValues[1];

        final double newM = oldM + ((value - oldM) / getCount());
        final double newS = oldS + ((value - oldM) * (value - newM));

        newValues[0] = newM;
        newValues[1] = newS;
      }
      done = variance.compareAndSet(oldValues, newValues);
      if (done) {
        // recycle the old array into the cache
        arrayCache.set(oldValues);
      }
    }
  }

}
