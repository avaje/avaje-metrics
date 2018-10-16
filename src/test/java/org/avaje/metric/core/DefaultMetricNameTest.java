package org.avaje.metric.core;

import org.avaje.metric.MetricName;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class DefaultMetricNameTest {

  @Test
  public void testParse() {

    MetricName name = new DefaultMetricName("org.test.Hello.rob");

    assertNotNull(name);
    assertEquals("org.test.Hello.rob", name.getSimpleName());
  }

  @Test
  public void partial_onlyTwo() {

    MetricName name =  new DefaultMetricName("Hello.rob");

    assertNotNull(name);
    assertEquals("Hello.rob", name.getSimpleName());
  }

  @Test
  public void partial_onlyOne() {

    MetricName name = new DefaultMetricName("test");
    assertEquals("test", name.getSimpleName());
  }

  @Test
  public void startsWith() {

    MetricName m0 = new DefaultMetricName("web.api.Hello.rob");
    MetricName m1 = new DefaultMetricName("web.api.some.Foo.bar");

    assertTrue(m0.startsWith("web.api"));
    assertTrue(m1.startsWith("web.api"));

    assertFalse(new DefaultMetricName("web.ap.some.Foo.bar").startsWith("web.api"));
    assertFalse(new DefaultMetricName("web.ap").startsWith("web.api"));

  }

  @Test
  public void isError() {

    MetricName m0 = new DefaultMetricName("web.api.Hello.rob.error");
    MetricName m1 = new DefaultMetricName("web.api.some.Foo.bar.err");

    assertTrue(m0.isError());
    assertFalse(m1.isError());
  }
}
