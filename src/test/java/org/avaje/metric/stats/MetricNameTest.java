package org.avaje.metric.stats;

import javax.management.ObjectName;

import org.avaje.metric.MetricName;
import org.junit.Assert;
import org.junit.Test;

public class MetricNameTest {

  @Test
  public void testType() {
    
    MetricName name = new MetricName("log","t","error");
    
    Assert.assertNotNull(name);
    ObjectName mBeanObjectName = name.getMBeanObjectName();
    Assert.assertNotNull(mBeanObjectName);
    
  }
}
