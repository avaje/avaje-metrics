package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricName;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

final class JvmThreadMetricGroup {

  static List<Metric> createThreadMetricGroup(boolean reportChangesOnly, boolean withDetails) {
    ThreadGauges threadGauges = new ThreadGauges(ManagementFactory.getThreadMXBean());
    return threadGauges.createMetrics(reportChangesOnly, withDetails);
  }

  private static class ThreadGauges {

    private final ThreadMXBean threadMXBean;

    ThreadGauges(ThreadMXBean threadMXBean) {
      this.threadMXBean = threadMXBean;
    }

    public List<Metric> createMetrics(boolean reportChangesOnly, boolean withDetails) {
      MetricName baseName = new DefaultMetricName("jvm.threads");
      List<Metric> metrics = new ArrayList<>(3);

      metrics.add(new DefaultGaugeLongMetric(baseName.append("current"), new Count(threadMXBean), reportChangesOnly));
      if (withDetails) {
        metrics.add(new DefaultGaugeLongMetric(baseName.append("peak"), new Peak(threadMXBean), reportChangesOnly));
        metrics.add(new DefaultGaugeLongMetric(baseName.append("daemon"), new Daemon(threadMXBean), reportChangesOnly));
      }
      return metrics;
    }

    static class Count implements GaugeLong {
      private final ThreadMXBean threadMXBean;

      Count(ThreadMXBean threadMXBean) {
        this.threadMXBean = threadMXBean;
      }

      @Override
      public long getValue() {
        return threadMXBean.getThreadCount();
      }
    }

    static class Peak implements GaugeLong {
      private final ThreadMXBean threadMXBean;

      Peak(ThreadMXBean threadMXBean) {
        this.threadMXBean = threadMXBean;
      }

      @Override
      public long getValue() {
        // read and reset the peak
        int peakThreadCount = threadMXBean.getPeakThreadCount();
        threadMXBean.resetPeakThreadCount();
        return peakThreadCount;
      }
    }

    static class Daemon implements GaugeLong {
      private final ThreadMXBean threadMXBean;

      Daemon(ThreadMXBean threadMXBean) {
        this.threadMXBean = threadMXBean;
      }

      @Override
      public long getValue() {
        return threadMXBean.getDaemonThreadCount();
      }
    }
  }
}
