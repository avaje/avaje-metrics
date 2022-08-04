package io.avaje.metrics.core;

import io.avaje.metrics.MetricName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DefaultMetricNameCacheTest {

  @Test
  void test() {

    DMetricNameCache cache = new DMetricNameCache(DefaultMetricNameCacheTest.class);

    MetricName metricName = cache.get("foo");
    assertEquals("io.avaje.metrics.core.DefaultMetricNameCacheTest.foo", metricName.simpleName());

    MetricName metricName2 = cache.get("bar");
    assertEquals("io.avaje.metrics.core.DefaultMetricNameCacheTest.bar", metricName2.simpleName());

    MetricName metricName3 = cache.get("foo");
    assertSame(metricName, metricName3);
  }
}
