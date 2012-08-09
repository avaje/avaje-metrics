package org.avaje.metric.jvm;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.avaje.metric.MetricName;
import org.avaje.metric.LoadMetric;

/**
 * Collect statistics on the rate of garbage collection.
 */
public class GarbageCollectionRateCollection {

  private final Collector[] collectors;
  private final LoadMetric[] gcLoadMetrics;

  public GarbageCollectionRateCollection(Timer timer) {

    List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory
        .getGarbageCollectorMXBeans();

    collectors = new Collector[garbageCollectorMXBeans.size()];
    gcLoadMetrics = new LoadMetric[garbageCollectorMXBeans.size()];

    for (int i = 0; i < garbageCollectorMXBeans.size(); i++) {
      GarbageCollectorMXBean garbageCollectorMXBean = garbageCollectorMXBeans.get(i);
      collectors[i] = new Collector(garbageCollectorMXBean);
      gcLoadMetrics[i] = collectors[i].getGCLoadMetric();
    }

    timer.scheduleAtFixedRate(new CollectTask(), 20000, 20000);
  }

  public String toString() {
    return Arrays.toString(collectors);
  }

  public LoadMetric[] getGarbageCollectorsLoadMetrics() {
    return gcLoadMetrics;
  }

  private void collectGcStats() {
    synchronized (this) {
      for (int i = 0; i < collectors.length; i++) {
        collectors[i].collect();
      }
    }
  }

  private class CollectTask extends TimerTask {
    @Override
    public void run() {
      collectGcStats();
    }
  }

  /**
   * A per garbage collector collector.
   */
  private static class Collector {

    final GarbageCollectorMXBean garbageCollectorMXBean;

    final LoadMetric gcLoadMetric;

    final AtomicLong lastCollectionCount = new AtomicLong();
    final AtomicLong lastCollectionTime = new AtomicLong();

    Collector(GarbageCollectorMXBean garbageCollectorMXBean) {
      this.garbageCollectorMXBean = garbageCollectorMXBean;

      String name = garbageCollectorMXBean.getName().toLowerCase();
      MetricName gcCountName = new MetricName("jvm", "gc", name);

      gcLoadMetric = new LoadMetric(gcCountName, TimeUnit.MINUTES, "gc", "ms");
    }

    private void collect() {

      long collectionCount = garbageCollectorMXBean.getCollectionCount();
      long collectionTime = garbageCollectorMXBean.getCollectionTime();

      long gcCountSince = collectionCount - lastCollectionCount.get();
      long gcDurationMillisSince = collectionTime - lastCollectionTime.get();

      gcLoadMetric.addEvent(gcCountSince, gcDurationMillisSince);
      // System.out.println("GC - "+gcLoadMetric);

      lastCollectionCount.set(collectionCount);
      lastCollectionTime.set(collectionTime);
    }

    public LoadMetric getGCLoadMetric() {
      return gcLoadMetric;
    }

    public String toString() {
      return gcLoadMetric.toString();
    }

  }
}