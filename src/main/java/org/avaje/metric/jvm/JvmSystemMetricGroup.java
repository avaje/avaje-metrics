package org.avaje.metric.jvm;

import org.avaje.metric.GaugeDouble;
import org.avaje.metric.core.DefaultGaugeDoubleMetric;
import org.avaje.metric.core.DefaultMetricName;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class JvmSystemMetricGroup {

  public static DefaultGaugeDoubleMetric getOsLoadAvgMetric() {
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
