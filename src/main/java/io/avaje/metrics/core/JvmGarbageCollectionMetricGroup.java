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

  static List<Metric> createGauges() {

    List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();

    List<Metric> metrics = new ArrayList<>();

    for (GarbageCollectorMXBean gcMXBean : garbageCollectorMXBeans) {
      // modify collector name replacing spaces with hyphens.
      String gcName = gcMXBean.getName().toLowerCase().replace(' ', '-');
      MetricName baseName = new DefaultMetricName("jvm.gc." + gcName);
      metrics.add(DefaultGaugeLongMetric.incrementing(baseName.append("count"), new Count(gcMXBean)));
      metrics.add(DefaultGaugeLongMetric.incrementing(baseName.append("time"), new Time(gcMXBean)));
    }

    return metrics;
  }


  private static class Count implements GaugeLong {

    final GarbageCollectorMXBean gcMXBean;

    Count(GarbageCollectorMXBean gcMXBean) {
      this.gcMXBean = gcMXBean;
    }

    @Override
    public long getValue() {
      return gcMXBean.getCollectionCount();
    }
  }

  private static class Time implements GaugeLong {

    final GarbageCollectorMXBean gcMXBean;

    Time(GarbageCollectorMXBean gcMXBean) {
      this.gcMXBean = gcMXBean;
    }

    @Override
    public long getValue() {
      return gcMXBean.getCollectionTime();
    }
  }
}
