package org.avaje.metric.core;

import org.avaje.metric.MetricManager;
import org.avaje.metric.MetricName;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.TimedMetricGroup;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class TimedMetricGroupTest {

  @Test
  public void test() {


    TimedMetricGroup timedMetricGroup = MetricManager.getTimedMetricGroup(MetricName.of("org.test.Hello.dummy"));

    TimedMetric timedMetric = timedMetricGroup.getTimedMetric("one");
    assertEquals("org.test", timedMetric.getName().getGroup());
    assertEquals("Hello", timedMetric.getName().getType());
    assertEquals("one", timedMetric.getName().getName());

    TimedMetric timedMetric2 = timedMetricGroup.getTimedMetric("one");

    Assert.assertSame(timedMetric, timedMetric2);


    TimedMetric two = timedMetricGroup.getTimedMetric("two");
    Assert.assertNotSame(timedMetric, two);

    assertEquals("org.test", two.getName().getGroup());
    assertEquals("Hello", two.getName().getType());
    assertEquals("two", two.getName().getName());

  }
}
