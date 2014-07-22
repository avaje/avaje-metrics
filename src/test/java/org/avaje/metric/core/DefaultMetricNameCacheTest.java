package org.avaje.metric.core;

import org.avaje.metric.MetricName;
import org.junit.Assert;
import org.junit.Test;

public class DefaultMetricNameCacheTest {

  @Test
  public void test() {
    
    
    DefaultMetricNameCache cache = new DefaultMetricNameCache(DefaultMetricNameCacheTest.class);
    
    MetricName metricName = cache.get("foo");
    
    Assert.assertEquals("org.avaje.metric.core", metricName.getGroup());
    Assert.assertEquals("DefaultMetricNameCacheTest", metricName.getType());
    Assert.assertEquals("foo", metricName.getName());
    
    MetricName metricName2 = cache.get("bar");
    
    Assert.assertEquals("org.avaje.metric.core", metricName2.getGroup());
    Assert.assertEquals("DefaultMetricNameCacheTest", metricName2.getType());
    Assert.assertEquals("bar", metricName2.getName());

    MetricName metricName3 = cache.get("foo");

    Assert.assertSame(metricName, metricName3);
    
  }
}
