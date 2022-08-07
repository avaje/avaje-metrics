package io.avaje.metrics.core;

import io.avaje.metrics.Metric;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;

/**
 * Collect statistics on the rate of garbage collection.
 */
final class JvmGarbageCollectionMetricGroup {

  static List<Metric> createGauges(boolean withDetails) {
    List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();

    List<Metric> metrics = new ArrayList<>();
    metrics.add(createTotalGcTime(garbageCollectorMXBeans));
    if (withDetails) {
      for (GarbageCollectorMXBean gcMXBean : garbageCollectorMXBeans) {
        // modify collector name replacing spaces with hyphens.
        String gcName = gcMXBean.getName().toLowerCase().replace(' ', '-').replace(".", "");
        metrics.add(DGaugeLong.incrementing(name("count", gcName), new Count(gcMXBean)));
        metrics.add(DGaugeLong.incrementing(name("time", gcName), new Time(gcMXBean)));
      }
    }
    return metrics;
  }

  /**
   * Return a Gauge for the total GC time in millis. Gives us a single metric to measure aggregate GC activity.
   */
  private static DGaugeLong createTotalGcTime(List<GarbageCollectorMXBean> garbageCollectorMXBeans) {
    GarbageCollectorMXBean[] gcBeans = garbageCollectorMXBeans.toArray(new GarbageCollectorMXBean[0]);
    return DGaugeLong.incrementing("jvm.gc.time", new TotalTime(gcBeans));
  }

  private static String name(String prefix, String gcName) {
    return "jvm.gc." + prefix + "." + gcName;
  }

  private static final class Count implements LongSupplier {

    final GarbageCollectorMXBean gcMXBean;

    Count(GarbageCollectorMXBean gcMXBean) {
      this.gcMXBean = gcMXBean;
    }

    @Override
    public long getAsLong() {
      return gcMXBean.getCollectionCount();
    }
  }

  private static final class Time implements LongSupplier {

    final GarbageCollectorMXBean gcMXBean;

    Time(GarbageCollectorMXBean gcMXBean) {
      this.gcMXBean = gcMXBean;
    }

    @Override
    public long getAsLong() {
      return gcMXBean.getCollectionTime();
    }
  }

  private static final class TotalTime implements LongSupplier {

    final GarbageCollectorMXBean[] gcMXBeans;

    TotalTime(GarbageCollectorMXBean[] gcMXBeans) {
      this.gcMXBeans = gcMXBeans;
    }

    @Override
    public long getAsLong() {
      long total = 0;
      for (GarbageCollectorMXBean gcMXBean : gcMXBeans) {
        total += gcMXBean.getCollectionTime();
      }
      return total;
    }
  }
}
