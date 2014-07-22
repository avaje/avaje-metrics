package org.avaje.metric.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import org.avaje.metric.GaugeCounter;
import org.avaje.metric.GaugeCounterMetric;
import org.avaje.metric.GaugeCounterMetricGroup;
import org.avaje.metric.core.DefaultGaugeCounterMetric;
import org.avaje.metric.core.DefaultGaugeCounterMetricGroup;
import org.avaje.metric.core.DefaultMetricName;

public final class JvmThreadMetricGroup {

  public static GaugeCounterMetricGroup createThreadMetricGroup() {
    
    GaugeCounter[] gauges = new ThreadGauges(ManagementFactory.getThreadMXBean()).getGauges();

    DefaultMetricName baseName = DefaultMetricName.createBaseName("jvm", "threads");
    GaugeCounterMetric[] metrics = new GaugeCounterMetric[3];
    metrics[0] = new DefaultGaugeCounterMetric(baseName.deriveWithName("current"), gauges[0]);
    metrics[1] = new DefaultGaugeCounterMetric(baseName.deriveWithName("peak"), gauges[1]);
    metrics[2] = new DefaultGaugeCounterMetric(baseName.deriveWithName("daemon"), gauges[2]);

    return new DefaultGaugeCounterMetricGroup(baseName, metrics);
  }


  private static class ThreadGauges {
    
    private final ThreadMXBean threadMXBean;
    private final GaugeCounter[] gauges = new GaugeCounter[3];

    ThreadGauges(ThreadMXBean threadMXBean) {
      this.threadMXBean = threadMXBean;
      gauges[0] = new Count();
      gauges[1] = new Peak();
      gauges[2] = new Daemon();
    }

    GaugeCounter[] getGauges() {
      return gauges;
    }

    class Count implements GaugeCounter {
      @Override
      public long getValue() {
        return threadMXBean.getThreadCount();
      }
    }

    class Peak implements GaugeCounter {
      @Override
      public long getValue() {
        // read and reset the peak
        int peakThreadCount = threadMXBean.getPeakThreadCount();
        threadMXBean.resetPeakThreadCount(); 
        return peakThreadCount;
      }
    }

    class Daemon implements GaugeCounter {
      @Override
      public long getValue() {
        return threadMXBean.getDaemonThreadCount();
      }
    }
  }
}
