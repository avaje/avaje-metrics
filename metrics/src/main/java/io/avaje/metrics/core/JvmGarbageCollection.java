package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.Tags;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.function.LongSupplier;

/**
 * Collect statistics on the rate of garbage collection.
 */
final class JvmGarbageCollection {

  static void createGauges(MetricRegistry registry, boolean withDetails, Tags globalTags) {
    List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
    createTotalGcTime(registry, gcMXBeans, globalTags);
    if (withDetails) {
      for (GarbageCollectorMXBean gcMXBean : gcMXBeans) {
        // modify collector name replacing spaces with hyphens.
        String gcName = gcMXBean.getName().toLowerCase().replace(' ', '_').replace(".", "");
        registry.gauge(name("count", gcName), globalTags, GaugeLong.incrementing(new Count(gcMXBean)));
        registry.gauge(name("time", gcName), globalTags, GaugeLong.incrementing(new Time(gcMXBean)));
      }
    }
  }

  /**
   * Return a Gauge for the total GC time in millis. Gives us a single metric to measure aggregate GC activity.
   */
  private static void createTotalGcTime(MetricRegistry registry, List<GarbageCollectorMXBean> garbageCollectorMXBeans, Tags globalTags) {
    GarbageCollectorMXBean[] gcBeans = garbageCollectorMXBeans.toArray(new GarbageCollectorMXBean[0]);
    registry.gauge("jvm.gc.time", globalTags, GaugeLong.incrementing(new TotalTime(gcBeans)));
  }

  private static String name(String prefix, String gcName) {
    return "jvm.gc." + prefix + "." + gcName;
  }

  private static final class Count implements LongSupplier {

    private final GarbageCollectorMXBean gcMXBean;

    Count(GarbageCollectorMXBean gcMXBean) {
      this.gcMXBean = gcMXBean;
    }

    @Override
    public long getAsLong() {
      return gcMXBean.getCollectionCount();
    }
  }

  private static final class Time implements LongSupplier {

    private final GarbageCollectorMXBean gcMXBean;

    Time(GarbageCollectorMXBean gcMXBean) {
      this.gcMXBean = gcMXBean;
    }

    @Override
    public long getAsLong() {
      return gcMXBean.getCollectionTime();
    }
  }

  private static final class TotalTime implements LongSupplier {

    private final GarbageCollectorMXBean[] gcMXBeans;

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
