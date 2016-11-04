package org.avaje.metric.core;

import org.avaje.metric.ValueStatistics;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ValueCounterTest {

  @Test
  public void testGetStatisticsWithNoReset() {

    ValueCounter counter = new ValueCounter();
    Assert.assertEquals(Long.MIN_VALUE, counter.getMax());

    counter.add(100);

    ValueStatistics statistics = counter.getStatistics(false);
    Assert.assertEquals(1, statistics.getCount());
    Assert.assertEquals(100, statistics.getTotal());
    Assert.assertEquals(100, statistics.getMax());

    counter.add(50);
    // no activity, just get statistics again
    statistics = counter.getStatistics(false);
    Assert.assertEquals(2, statistics.getCount());
    Assert.assertEquals(150, statistics.getTotal());
    Assert.assertEquals(100, statistics.getMax());

  }

  @Test
  public void test() {
    
    ValueCounter counter = new ValueCounter();

    Assert.assertEquals(0, counter.getCount());
    Assert.assertEquals(0, counter.getTotal());
    Assert.assertEquals(Long.MIN_VALUE, counter.getMax());

    counter.add(100);
    Assert.assertEquals(1, counter.getCount());
    Assert.assertEquals(100, counter.getTotal());
    Assert.assertEquals(100, counter.getMax());

    counter.add(50);
    Assert.assertEquals(2, counter.getCount());
    Assert.assertEquals(150, counter.getTotal());
    Assert.assertEquals(100, counter.getMax());

    counter.add(200);
    Assert.assertEquals(3, counter.getCount());
    Assert.assertEquals(350, counter.getTotal());
    Assert.assertEquals(200, counter.getMax());

    counter.add(20);
    Assert.assertEquals(4, counter.getCount());
    Assert.assertEquals(370, counter.getTotal());
    Assert.assertEquals(200, counter.getMax());
    
    counter.reset();
    Assert.assertEquals(0, counter.getCount());
    Assert.assertEquals(0, counter.getTotal());
    Assert.assertEquals(Long.MIN_VALUE, counter.getMax());
    
  }
}
