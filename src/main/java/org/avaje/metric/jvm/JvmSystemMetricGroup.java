package org.avaje.metric.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

import org.avaje.metric.Gauge;
import org.avaje.metric.GaugeMetric;

public class JvmSystemMetricGroup {

  public static GaugeMetric getUptime() {
    Gauge uptime = new UptimeGauge(ManagementFactory.getRuntimeMXBean());
    return new GaugeMetric("jvm", "system", "uptime", uptime);
  }
  
  public static GaugeMetric getOsLoadAvgMetric() {
    Gauge osLoadAvg = new OsLoadGauge(ManagementFactory.getOperatingSystemMXBean()); 
    return new GaugeMetric("jvm", "os", "loadAverage", osLoadAvg);
  }

  private static class UptimeGauge implements Gauge {
    
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
  
  private static class OsLoadGauge implements Gauge {
    
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
