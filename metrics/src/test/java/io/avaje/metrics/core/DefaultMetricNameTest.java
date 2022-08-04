package io.avaje.metrics.core;

import io.avaje.metrics.MetricName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultMetricNameTest {

  @Test
  void testParse() {

    MetricName name = new DMetricName("org.test.Hello.rob");

    assertNotNull(name);
    assertEquals("org.test.Hello.rob", name.simpleName());
  }

  @Test
  void partial_onlyTwo() {

    MetricName name = new DMetricName("Hello.rob");

    assertNotNull(name);
    assertEquals("Hello.rob", name.simpleName());
  }

  @Test
  void partial_onlyOne() {

    MetricName name = new DMetricName("test");
    assertEquals("test", name.simpleName());
  }

  @Test
  void startsWith() {

    MetricName m0 = new DMetricName("web.api.Hello.rob");
    MetricName m1 = new DMetricName("web.api.some.Foo.bar");

    assertTrue(m0.startsWith("web.api"));
    assertTrue(m1.startsWith("web.api"));

    assertFalse(new DMetricName("web.ap.some.Foo.bar").startsWith("web.api"));
    assertFalse(new DMetricName("web.ap").startsWith("web.api"));

  }

  @Test
  void isError() {

    MetricName m0 = new DMetricName("web.api.Hello.rob.error");
    MetricName m1 = new DMetricName("web.api.some.Foo.bar.err");

    assertTrue(m0.isError());
    assertFalse(m1.isError());
  }
}
