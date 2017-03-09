package org.avaje.metric.jvm;

import org.avaje.metric.GaugeLong;
import org.avaje.metric.Metric;
import org.avaje.metric.core.DefaultGaugeLongMetric;
import org.avaje.metric.core.DefaultMetricName;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Collect statistics on the rate of garbage collection.
 */
public final class JvmGarbageCollectionMetricGroup {

  public static List<Metric> createGauges() {

    List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();

    List<Metric> metrics = new ArrayList<>();

    for (GarbageCollectorMXBean gcMXBean : garbageCollectorMXBeans) {
      // modify collector name replacing spaces with hyphens.
      String gcName = gcMXBean.getName().toLowerCase().replace(' ', '-');
      DefaultMetricName baseName = DefaultMetricName.createBaseName("jvm.gc", gcName);

      metrics.add(DefaultGaugeLongMetric.incrementing(baseName.withName("count"), new Count(gcMXBean)));
      metrics.add(DefaultGaugeLongMetric.incrementing(baseName.withName("time"), new Time(gcMXBean)));
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
