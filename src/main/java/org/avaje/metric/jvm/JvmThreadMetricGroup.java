package org.avaje.metric.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import org.avaje.metric.Gauge;
import org.avaje.metric.GaugeMetric;
import org.avaje.metric.GaugeMetricGroup;
import org.avaje.metric.MetricName;

public final class JvmThreadMetricGroup {

  private final GaugeMetricGroup group;

  public JvmThreadMetricGroup() {
    
    Gauge[] gauges = new ThreadGauges(ManagementFactory.getThreadMXBean()).getGauges();

    MetricName baseName = MetricName.createBaseName("jvm", "threads");
    GaugeMetric[] metrics = new GaugeMetric[3];
    metrics[0] = new GaugeMetric(baseName.deriveWithName("current"), gauges[0], true);
    metrics[1] = new GaugeMetric(baseName.deriveWithName("peak"), gauges[1], true);
    metrics[2] = new GaugeMetric(baseName.deriveWithName("daemon"), gauges[2], true);

    group = new GaugeMetricGroup(baseName, metrics);
  }

  public GaugeMetricGroup getGaugeMetricGroup() {
    return group;
  }

  class ThreadGauges {
    
    final ThreadMXBean threadMXBean;
    final Gauge[] gauges = new Gauge[3];

    ThreadGauges(ThreadMXBean threadMXBean) {
      this.threadMXBean = threadMXBean;
      gauges[0] = new Count();
      gauges[1] = new Peak();
      gauges[2] = new Daemon();
    }

    public Gauge[] getGauges() {
      return gauges;
    }

    class Count implements Gauge {
      @Override
      public double getValue() {
        return threadMXBean.getThreadCount();
      }
    }

    class Peak implements Gauge {
      @Override
      public double getValue() {
        // read and reset the peak
        int peakThreadCount = threadMXBean.getPeakThreadCount();
        threadMXBean.resetPeakThreadCount(); 
        return peakThreadCount;
      }
    }

    class Daemon implements Gauge {
      @Override
      public double getValue() {
        return threadMXBean.getDaemonThreadCount();
      }
    }
  }
}
