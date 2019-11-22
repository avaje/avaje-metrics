package io.avaje.metrics.core;

import io.avaje.metrics.MetricName;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

public class DefaultMetricNameCacheTest {

  @Test
  public void test() {


    DefaultMetricNameCache cache = new DefaultMetricNameCache(DefaultMetricNameCacheTest.class);

    MetricName metricName = cache.get("foo");
    assertEquals("io.avaje.metrics.core.DefaultMetricNameCacheTest.foo", metricName.getSimpleName());

    MetricName metricName2 = cache.get("bar");
    assertEquals("io.avaje.metrics.core.DefaultMetricNameCacheTest.bar", metricName2.getSimpleName());

    MetricName metricName3 = cache.get("foo");
    assertSame(metricName, metricName3);

  }
}
