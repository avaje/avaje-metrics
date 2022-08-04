package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricName;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

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
        metrics.add(DGaugeLongMetric.incrementing(name("count", gcName), new Count(gcMXBean)));
        metrics.add(DGaugeLongMetric.incrementing(name("time", gcName), new Time(gcMXBean)));
      }
    }
    return metrics;
  }

  /**
   * Return a Gauge for the total GC time in millis. Gives us a single metric to measure aggregate GC activity.
   */
  private static DGaugeLongMetric createTotalGcTime(List<GarbageCollectorMXBean> garbageCollectorMXBeans) {
    GarbageCollectorMXBean[] gcBeans = garbageCollectorMXBeans.toArray(new GarbageCollectorMXBean[0]);
    return DGaugeLongMetric.incrementing(new DMetricName("jvm.gc.time"), new TotalTime(gcBeans));
  }

  private static MetricName name(String prefix, String gcName) {
    return new DMetricName("jvm.gc." + prefix + "." + gcName);
  }

  private static final class Count implements GaugeLong {

    final GarbageCollectorMXBean gcMXBean;

    Count(GarbageCollectorMXBean gcMXBean) {
      this.gcMXBean = gcMXBean;
    }

    @Override
    public long value() {
      return gcMXBean.getCollectionCount();
    }
  }

  private static final class Time implements GaugeLong {

    final GarbageCollectorMXBean gcMXBean;

    Time(GarbageCollectorMXBean gcMXBean) {
      this.gcMXBean = gcMXBean;
    }

    @Override
    public long value() {
      return gcMXBean.getCollectionTime();
    }
  }

  private static final class TotalTime implements GaugeLong {

    final GarbageCollectorMXBean[] gcMXBeans;

    TotalTime(GarbageCollectorMXBean[] gcMXBeans) {
      this.gcMXBeans = gcMXBeans;
    }

    @Override
    public long value() {
      long total = 0;
      for (GarbageCollectorMXBean gcMXBean : gcMXBeans) {
        total += gcMXBean.getCollectionTime();
      }
      return total;
    }
  }
}
