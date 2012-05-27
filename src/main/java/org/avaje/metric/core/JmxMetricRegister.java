package org.avaje.metric.core;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.avaje.metric.MetricStatistics;
import org.avaje.metric.MetricRateStatistics;

public final class JmxMetricRegister {

  private MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();

  public JmxMetricRegister() {
  }

  public RateStatisticsMXBean createMetricMXBean(MetricRateStatistics stats){
    return new MxRateStatistics(stats);
  }
  
  public MetricMXBean createMetricMXBean(MetricStatistics stats){
    return new MxMetric(stats);
  }
  
  public void register(RateStatisticsMXBean mbean, ObjectName name) {
    registerMbean(mbean, name);
  }
  
  public void register(MetricMXBean mbean, ObjectName name) {
    registerMbean(mbean, name);
  }
  
  private void registerMbean(Object mbean, ObjectName name) {
    try {
      platformMBeanServer.registerMBean(mbean, name);

    } catch (InstanceAlreadyExistsException e) {
      throw new IllegalArgumentException(e);
    } catch (MBeanRegistrationException e) {
      throw new IllegalArgumentException(e);
    } catch (NotCompliantMBeanException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public void unregister(ObjectName mBeanObjectName) {
    try {
      platformMBeanServer.unregisterMBean(mBeanObjectName);

    } catch (MBeanRegistrationException e) {
      throw new IllegalStateException(e);
    } catch (InstanceNotFoundException e) {
      throw new IllegalStateException(e);
    }

  }

}
