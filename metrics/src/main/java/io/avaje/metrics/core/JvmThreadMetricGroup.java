package io.avaje.metrics.core;

import io.avaje.metrics.Metric;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;

final class JvmThreadMetricGroup {

  static List<Metric> createThreadMetricGroup(boolean reportChangesOnly, boolean withDetails) {
    ThreadGauges threadGauges = new ThreadGauges(ManagementFactory.getThreadMXBean());
    return threadGauges.createMetrics(reportChangesOnly, withDetails);
  }

  private static final class ThreadGauges {

    private final ThreadMXBean threadMXBean;

    ThreadGauges(ThreadMXBean threadMXBean) {
      this.threadMXBean = threadMXBean;
    }

    List<Metric> createMetrics(boolean reportChangesOnly, boolean withDetails) {
      List<Metric> metrics = new ArrayList<>(3);

      metrics.add(new DGaugeLongMetric("jvm.threads.current", new Count(threadMXBean), reportChangesOnly));
      if (withDetails) {
        metrics.add(new DGaugeLongMetric("jvm.threads.peak", new Peak(threadMXBean), reportChangesOnly));
        metrics.add(new DGaugeLongMetric("jvm.threads.daemon", new Daemon(threadMXBean), reportChangesOnly));
      }
      return metrics;
    }

    static final class Count implements LongSupplier {
      private final ThreadMXBean threadMXBean;

      Count(ThreadMXBean threadMXBean) {
        this.threadMXBean = threadMXBean;
      }

      @Override
      public long getAsLong() {
        return threadMXBean.getThreadCount();
      }
    }

    static final class Peak implements LongSupplier {
      private final ThreadMXBean threadMXBean;

      Peak(ThreadMXBean threadMXBean) {
        this.threadMXBean = threadMXBean;
      }

      @Override
      public long getAsLong() {
        // read and reset the peak
        int peakThreadCount = threadMXBean.getPeakThreadCount();
        threadMXBean.resetPeakThreadCount();
        return peakThreadCount;
      }
    }

    static final class Daemon implements LongSupplier {
      private final ThreadMXBean threadMXBean;

      Daemon(ThreadMXBean threadMXBean) {
        this.threadMXBean = threadMXBean;
      }

      @Override
      public long getAsLong() {
        return threadMXBean.getDaemonThreadCount();
      }
    }
  }
}
