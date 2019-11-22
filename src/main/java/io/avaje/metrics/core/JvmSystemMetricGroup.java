package io.avaje.metrics.core;

import io.avaje.metrics.GaugeDouble;
import io.avaje.metrics.GaugeDoubleMetric;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

class JvmSystemMetricGroup {

  static GaugeDoubleMetric getOsLoadAvgMetric() {
    GaugeDouble osLoadAvg = new OsLoadGauge(ManagementFactory.getOperatingSystemMXBean());
    return new DefaultGaugeDoubleMetric(new DefaultMetricName("jvm.os.loadAverage"), osLoadAvg);
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
