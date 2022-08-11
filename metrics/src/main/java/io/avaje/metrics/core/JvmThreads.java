package io.avaje.metrics.core;

import io.avaje.metrics.MetricRegistry;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.function.LongSupplier;

final class JvmThreads {

  static void createThreadMetricGroup(MetricRegistry registry, boolean reportChangesOnly, boolean withDetails) {
    ThreadGauges threadGauges = new ThreadGauges(ManagementFactory.getThreadMXBean());
    threadGauges.createMetrics(registry, reportChangesOnly, withDetails);
  }

  private static final class ThreadGauges {

    private final ThreadMXBean threadMXBean;

    ThreadGauges(ThreadMXBean threadMXBean) {
      this.threadMXBean = threadMXBean;
    }

    void createMetrics(MetricRegistry registry, boolean reportChangesOnly, boolean withDetails) {
      registry.register(DGaugeLong.of("jvm.threads.current", new Count(threadMXBean), reportChangesOnly));
      if (withDetails) {
        registry.register(DGaugeLong.of("jvm.threads.peak", new Peak(threadMXBean), reportChangesOnly));
        registry.register(DGaugeLong.of("jvm.threads.daemon", new Daemon(threadMXBean), reportChangesOnly));
      }
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
