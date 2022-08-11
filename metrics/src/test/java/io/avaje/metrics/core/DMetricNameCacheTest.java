package io.avaje.metrics.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DMetricNameCacheTest {

  @Test
  void test() {
    DMetricNameCache cache = new DMetricNameCache(DMetricNameCacheTest.class);

    String metricName = cache.get("foo");
    assertEquals("io.avaje.metrics.core.DMetricNameCacheTest.foo", metricName);

    String metricName2 = cache.get("bar");
    assertEquals("io.avaje.metrics.core.DMetricNameCacheTest.bar", metricName2);

    String metricName3 = cache.get("foo");
    assertSame(metricName, metricName3);
  }
}
