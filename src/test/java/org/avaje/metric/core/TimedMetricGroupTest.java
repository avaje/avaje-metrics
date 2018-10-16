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


    TimedMetricGroup timedMetricGroup = MetricManager.getTimedMetricGroup(MetricName.of("org.test.Hello"));

    TimedMetric timedMetric = timedMetricGroup.getTimedMetric("one");
    assertEquals("org.test.Hello.one", timedMetric.getName().getSimpleName());


    TimedMetric timedMetric2 = timedMetricGroup.getTimedMetric("one");

    Assert.assertSame(timedMetric, timedMetric2);


    TimedMetric two = timedMetricGroup.getTimedMetric("two");
    Assert.assertNotSame(timedMetric, two);

    assertEquals("org.test.Hello.two", two.getName().getSimpleName());

  }
}
