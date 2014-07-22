package org.avaje.metric.core;

import org.avaje.metric.CounterMetric;
import org.avaje.metric.CounterStatistics;
import org.avaje.metric.MetricManager;
import org.junit.Assert;
import org.junit.Test;

public class CounterMetricTest {

  @Test
  public void test() {
    
    CounterMetric counterMetric = MetricManager.getCounterMetric(new DefaultMetricName("org", "test", "mycountermetric"));
    
    Assert.assertEquals("org",counterMetric.getName().getGroup());
    Assert.assertEquals("test",counterMetric.getName().getType());
    Assert.assertEquals("mycountermetric",counterMetric.getName().getName());
     
    counterMetric.clearStatistics();
    Assert.assertEquals(0, counterMetric.getStatistics(false).getCount());

    counterMetric.markEvent();
    Assert.assertEquals(1, counterMetric.getStatistics(false).getCount());
    counterMetric.markEvent();
    Assert.assertEquals(2, counterMetric.getStatistics(false).getCount());
    counterMetric.markEvent();
    Assert.assertEquals(3, counterMetric.getStatistics(false).getCount());
    counterMetric.markEvents(100);
    Assert.assertEquals(103, counterMetric.getStatistics(false).getCount());
    
    Assert.assertTrue(counterMetric.collectStatistics());
    CounterStatistics collectedStatistics = counterMetric.getCollectedStatistics();
    Assert.assertEquals(103, collectedStatistics.getCount());

    
    Assert.assertEquals(0, counterMetric.getStatistics(false).getCount());

  }
}
