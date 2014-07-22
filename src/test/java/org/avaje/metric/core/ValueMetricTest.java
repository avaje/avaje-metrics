package org.avaje.metric.core;

import org.avaje.metric.MetricManager;
import org.avaje.metric.ValueMetric;
import org.avaje.metric.ValueStatistics;
import org.junit.Assert;
import org.junit.Test;

public class ValueMetricTest {

  @Test
  public void test() {

    ValueMetric metric = MetricManager.getValueMetric(new DefaultMetricName("org", "test", "mycounter"));

    Assert.assertEquals("org", metric.getName().getGroup());
    Assert.assertEquals("test", metric.getName().getType());
    Assert.assertEquals("mycounter", metric.getName().getName());

    metric.clearStatistics();

    metric.addEvent(1000);
    metric.addEvent(2000);
    metric.addEvent(1500);
    
    Assert.assertTrue(metric.collectStatistics());
    
    ValueStatistics statistics = metric.getCollectedStatistics();
    
    Assert.assertEquals(3, statistics.getCount());
    Assert.assertEquals(4500, statistics.getTotal());
    Assert.assertEquals(2000, statistics.getMax());
    Assert.assertEquals(1500, statistics.getMean());


    Assert.assertFalse(metric.collectStatistics());

  }
}
