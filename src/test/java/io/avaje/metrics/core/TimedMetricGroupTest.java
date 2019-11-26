package io.avaje.metrics.core;

import io.avaje.metrics.MetricManager;
import io.avaje.metrics.MetricName;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.TimedMetricGroup;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class TimedMetricGroupTest {

  @Test
  public void test() {


    TimedMetricGroup timedMetricGroup = MetricManager.timedGroup(MetricName.of("org.test.Hello"));

    TimedMetric timedMetric = timedMetricGroup.getTimedMetric("one");
    assertEquals("org.test.Hello.one", timedMetric.getName().getSimpleName());


    TimedMetric timedMetric2 = timedMetricGroup.getTimedMetric("one");

    Assert.assertSame(timedMetric, timedMetric2);


    TimedMetric two = timedMetricGroup.getTimedMetric("two");
    Assert.assertNotSame(timedMetric, two);

    assertEquals("org.test.Hello.two", two.getName().getSimpleName());

  }
}
