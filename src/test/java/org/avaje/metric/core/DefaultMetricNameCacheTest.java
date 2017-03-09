package org.avaje.metric.core;

import org.avaje.metric.MetricName;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

public class DefaultMetricNameCacheTest {

  @Test
  public void test() {
    
    
    DefaultMetricNameCache cache = new DefaultMetricNameCache(DefaultMetricNameCacheTest.class);
    
    MetricName metricName = cache.get("foo");
    
    assertEquals("org.avaje.metric.core", metricName.getGroup());
    assertEquals("DefaultMetricNameCacheTest", metricName.getType());
    assertEquals("foo", metricName.getName());
    
    MetricName metricName2 = cache.get("bar");
    
    assertEquals("org.avaje.metric.core", metricName2.getGroup());
    assertEquals("DefaultMetricNameCacheTest", metricName2.getType());
    assertEquals("bar", metricName2.getName());

    MetricName metricName3 = cache.get("foo");

    assertSame(metricName, metricName3);
    
  }
}
