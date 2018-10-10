package org.avaje.metric.core;

import org.avaje.metric.GaugeDouble;
import org.avaje.metric.GaugeDoubleMetric;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

class JvmSystemMetricGroup {

  static GaugeDoubleMetric getOsLoadAvgMetric() {
    GaugeDouble osLoadAvg = new OsLoadGauge(ManagementFactory.getOperatingSystemMXBean());
    return new DefaultGaugeDoubleMetric(new DefaultMetricName("jvm", "os", "loadAverage"), osLoadAvg);
  }

  private static class OsLoadGauge implements GaugeDouble {

    private final OperatingSystemMXBean osMXbean;

    OsLoadGauge(OperatingSystemMXBean osMXbean) {
      this.osMXbean = osMXbean;
    }

    @Override
    public double getValue() {
      return osMXbean.getSystemLoadAverage();
    }

  }

}
