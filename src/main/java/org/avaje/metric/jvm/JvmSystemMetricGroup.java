package org.avaje.metric.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

import org.avaje.metric.GaugeDouble;
import org.avaje.metric.core.DefaultGaugeDoubleMetric;
import org.avaje.metric.core.DefaultMetricName;

public class JvmSystemMetricGroup {

  public static DefaultGaugeDoubleMetric getUptime() {
    GaugeDouble uptime = new UptimeGauge(ManagementFactory.getRuntimeMXBean());
    return new DefaultGaugeDoubleMetric(new DefaultMetricName("jvm", "system", "uptime"), uptime);
  }
  
  public static DefaultGaugeDoubleMetric getOsLoadAvgMetric() {
    GaugeDouble osLoadAvg = new OsLoadGauge(ManagementFactory.getOperatingSystemMXBean()); 
    return new DefaultGaugeDoubleMetric(new DefaultMetricName("jvm", "os", "loadAverage"), osLoadAvg);
  }

  private static class UptimeGauge implements GaugeDouble {
    
    private final RuntimeMXBean runtimeMXBean;
    
    UptimeGauge(RuntimeMXBean runtimeMXBean) {
      this.runtimeMXBean = runtimeMXBean;
    }
    
    @Override
    public double getValue() {
      long uptime = runtimeMXBean.getUptime();
      double uptimeMinutes = Math.floor(uptime / 60000);
      return uptimeMinutes;
    }
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
