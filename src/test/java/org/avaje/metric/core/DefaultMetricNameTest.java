package org.avaje.metric.core;

import org.avaje.metric.MetricName;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class DefaultMetricNameTest {

  @Test
  public void testParse() {

    MetricName name = DefaultMetricName.parse("org.test.Hello.rob");

    assertNotNull(name);
    assertEquals("org.test", name.getGroup());
    assertEquals("Hello", name.getType());
    assertEquals("rob", name.getName());

    MetricName name2 = DefaultMetricName.parse("test.Hello.rob");

    assertNotNull(name);
    assertEquals("test", name2.getGroup());
    assertEquals("Hello", name2.getType());
    assertEquals("rob", name2.getName());
  }

  @Test
  public void testFour_withError() {

    MetricName name = DefaultMetricName.parse("test.foo.Hello.r.error");

    assertEquals("test.foo", name.getGroup());
    assertEquals("Hello", name.getType());
    assertEquals("r.error", name.getName());
  }


  @Test
  public void testThree_withError() {

    MetricName name = DefaultMetricName.parse("test.Hello.r.error");

    assertEquals("test", name.getGroup());
    assertEquals("Hello", name.getType());
    assertEquals("r.error", name.getName());
  }


  @Test
  public void partial_onlyTwo() {

    MetricName name = DefaultMetricName.parse("Hello.rob");

    assertNotNull(name);
    assertEquals("Hello.rob", name.getSimpleName());
    assertEquals("Hello", name.getGroup());
    assertNull(name.getType());
    assertEquals("rob", name.getName());
  }

  @Test
  public void partial_onlyOne() {

    MetricName name = DefaultMetricName.parse("test");
    assertEquals("test", name.getSimpleName());

    assertEquals("test", name.getGroup());
    assertNull(name.getType());
    assertNull(name.getName());
  }

  @Test
  public void startsWith() {

    MetricName m0 = DefaultMetricName.parse("web.api.Hello.rob");
    MetricName m1 = DefaultMetricName.parse("web.api.some.Foo.bar");

    assertTrue(m0.startsWith("web.api"));
    assertTrue(m1.startsWith("web.api"));

    assertFalse(DefaultMetricName.parse("web.ap.some.Foo.bar").startsWith("web.api"));
    assertFalse(DefaultMetricName.parse("web.ap").startsWith("web.api"));

  }

  @Test
  public void isError() {

    MetricName m0 = DefaultMetricName.parse("web.api.Hello.rob.error");
    MetricName m1 = DefaultMetricName.parse("web.api.some.Foo.bar.err");

    assertTrue(m0.isError());
    assertFalse(m1.isError());
  }
}
