package org.avaje.metric.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import org.avaje.metric.GaugeLong;
import org.avaje.metric.GaugeLongGroup;
import org.avaje.metric.GaugeLongMetric;
import org.avaje.metric.core.DefaultGaugeLongMetric;
import org.avaje.metric.core.DefaultGaugeLongGroup;
import org.avaje.metric.core.DefaultMetricName;

public final class JvmThreadMetricGroup {

  public static GaugeLongGroup createThreadMetricGroup() {
    
    GaugeLong[] gauges = new ThreadGauges(ManagementFactory.getThreadMXBean()).getGauges();

    DefaultMetricName baseName = DefaultMetricName.createBaseName("jvm", "threads");
    GaugeLongMetric[] metrics = new GaugeLongMetric[3];
    metrics[0] = new DefaultGaugeLongMetric(baseName.deriveWithName("current"), gauges[0]);
    metrics[1] = new DefaultGaugeLongMetric(baseName.deriveWithName("peak"), gauges[1]);
    metrics[2] = new DefaultGaugeLongMetric(baseName.deriveWithName("daemon"), gauges[2]);

    return new DefaultGaugeLongGroup(baseName, metrics);
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
