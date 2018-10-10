package org.avaje.metric.core;

import org.avaje.metric.GaugeLong;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

final class JvmThreadMetricGroup {

  static List<Metric> createThreadMetricGroup() {

    GaugeLong[] gauges = new ThreadGauges(ManagementFactory.getThreadMXBean()).getGauges();

    MetricName baseName = new DefaultMetricName("jvm", "threads", "");

    List<Metric> metrics = new ArrayList<>(3);
    metrics.add(new DefaultGaugeLongMetric(baseName.withName("current"), gauges[0]));
    metrics.add(new DefaultGaugeLongMetric(baseName.withName("peak"), gauges[1]));
    metrics.add(new DefaultGaugeLongMetric(baseName.withName("daemon"), gauges[2]));
    return metrics;
  }


  private static class ThreadGauges {

    private final ThreadMXBean threadMXBean;
    private final GaugeLong[] gauges = new GaugeLong[3];

    ThreadGauges(ThreadMXBean threadMXBean) {
      this.threadMXBean = threadMXBean;
      gauges[0] = new Count();
      gauges[1] = new Peak();
      gauges[2] = new Daemon();
    }

    GaugeLong[] getGauges() {
      return gauges;
    }

    class Count implements GaugeLong {
      @Override
      public long getValue() {
        return threadMXBean.getThreadCount();
      }
    }

    class Peak implements GaugeLong {
      @Override
      public long getValue() {
        // read and reset the peak
        int peakThreadCount = threadMXBean.getPeakThreadCount();
        threadMXBean.resetPeakThreadCount();
        return peakThreadCount;
      }
    }

    class Daemon implements GaugeLong {
      @Override
      public long getValue() {
        return threadMXBean.getDaemonThreadCount();
      }
    }
  }
}
