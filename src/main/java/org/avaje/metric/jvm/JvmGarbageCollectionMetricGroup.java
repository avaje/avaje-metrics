package org.avaje.metric.jvm;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

import org.avaje.metric.Gauge;
import org.avaje.metric.GaugeMetric;
import org.avaje.metric.GaugeMetricGroup;
import org.avaje.metric.MetricName;

/**
 * Collect statistics on the rate of garbage collection.
 */
public final class JvmGarbageCollectionMetricGroup {

  private static String[] names = { "count", "time" };

  public static GaugeMetricGroup[] createGauges() {

    List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();

    GaugeMetricGroup[] metricGroups = new GaugeMetricGroup[garbageCollectorMXBeans.size()];

    for (int i = 0; i < garbageCollectorMXBeans.size(); i++) {
      GarbageCollectorMXBean gcMXBean = garbageCollectorMXBeans.get(i);

      // normalise the collector name
      String gcName = gcMXBean.getName();
      gcName = gcName.toLowerCase().replace(' ', '-');
      
      MetricName baseName = MetricName.createBaseName("jvm.gc", gcName);

      Gauge[] gauges = new Collector(gcMXBean).getGauges();
      GaugeMetric[] group = new GaugeMetric[gauges.length];

      for (int j = 0; j < gauges.length; j++) {
        MetricName metricName = baseName.deriveWithName(names[j]);
        group[j] = GaugeMetric.incrementing(metricName, gauges[j], true);
      }

      metricGroups[i] = new GaugeMetricGroup(baseName, group);
    }

    return metricGroups;
  }

  /**
   * A per garbage collector collector.
   */
  private static class Collector {

    final GarbageCollectorMXBean gcMXBean;
    final Gauge[] gauges;

    Collector(GarbageCollectorMXBean gcMXBean) {
      this.gcMXBean = gcMXBean;
      gauges = new Gauge[2];
      gauges[0] = new Count();
      gauges[1] = new Time();
    }

    public Gauge[] getGauges() {
      return gauges;
    }

    class Count implements Gauge {
      @Override
      public double getValue() {
        return gcMXBean.getCollectionCount();
      }
    }

    class Time implements Gauge {
      @Override
      public double getValue() {
        return gcMXBean.getCollectionTime();
      }
    }
  }
}
