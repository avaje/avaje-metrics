package org.avaje.metric.core;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.avaje.metric.Stats;

public final class JmxMetricRegister {

  private MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();

  public JmxMetricRegister() {
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
