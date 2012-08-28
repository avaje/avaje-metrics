package org.avaje.metric.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

public class JvmSystemMetricGroup {

  JvmSystemMetricGroup(){
    
    OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
    operatingSystemMXBean.getSystemLoadAverage();
    
    RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    runtimeMXBean.getUptime();
    
  }
}
