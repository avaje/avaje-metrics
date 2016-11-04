package org.avaje.metric.core;

import org.avaje.metric.MetricManager;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.TimedMetricGroup;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TimedMetricGroupTest {

  @Test
  public void test() {
    
    
    TimedMetricGroup timedMetricGroup = MetricManager.getTimedMetricGroup(MetricManager.name("org.test.Hello.dummy"));
    
    TimedMetric timedMetric = timedMetricGroup.getTimedMetric("one");
    Assert.assertEquals("org.test", timedMetric.getName().getGroup());
    Assert.assertEquals("Hello", timedMetric.getName().getType());
    Assert.assertEquals("one", timedMetric.getName().getName());
    
    TimedMetric timedMetric2 = timedMetricGroup.getTimedMetric("one");
    
    Assert.assertSame(timedMetric, timedMetric2);
    
    
    TimedMetric two = timedMetricGroup.getTimedMetric("two");
    Assert.assertNotSame(timedMetric, two);
    
    Assert.assertEquals("org.test", two.getName().getGroup());
    Assert.assertEquals("Hello", two.getName().getType());
    Assert.assertEquals("two", two.getName().getName());
    
  }
}
