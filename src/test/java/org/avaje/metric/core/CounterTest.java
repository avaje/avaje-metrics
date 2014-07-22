package org.avaje.metric.core;

import org.avaje.metric.CounterStatistics;
import org.junit.Assert;
import org.junit.Test;

public class CounterTest {

  @Test
  public void test() {
    
    Counter counter = new Counter();
    Assert.assertEquals(0, counter.getCount());
    Assert.assertTrue(counter.isEmpty());

    counter.increment();
    Assert.assertFalse(counter.isEmpty());
    Assert.assertEquals(1, counter.getCount());
    
    counter.increment();
    Assert.assertEquals(2, counter.getCount());
    counter.decrement();
    Assert.assertEquals(1, counter.getCount());
    counter.add(100);
    Assert.assertEquals(101, counter.getCount());
    
    CounterStatistics stats0 = counter.collectStatistics();
    Assert.assertEquals(101, stats0.getCount());
    Assert.assertTrue(counter.isEmpty());

    
    counter.add(101);
    Assert.assertEquals(101, counter.getCount());

    CounterStatistics stats1 = counter.getStatistics(true);
    Assert.assertEquals(101, stats1.getCount());
    
    Assert.assertTrue(counter.isEmpty());
    Assert.assertEquals(0, counter.getCount());
    

    counter.add(101);
    Assert.assertEquals(101, counter.getCount());

    CounterStatistics stats2 = counter.getStatistics(false);
    Assert.assertEquals(101, stats2.getCount());
    Assert.assertFalse(counter.isEmpty());
    Assert.assertEquals(101, counter.getCount());

  }
}
