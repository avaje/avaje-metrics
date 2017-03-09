package org.avaje.metric.core;

import org.avaje.metric.ValueStatistics;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ValueCounterTest {

  @Test
  public void testGetStatisticsWithNoReset() {

    ValueCounter counter = new ValueCounter();
    assertEquals(Long.MIN_VALUE, counter.getMax());

    counter.add(100);

    ValueStatistics statistics = counter.getStatistics(false);
    assertEquals(1, statistics.getCount());
    assertEquals(100, statistics.getTotal());
    assertEquals(100, statistics.getMax());

    counter.add(50);
    // no activity, just get statistics again
    statistics = counter.getStatistics(false);
    assertEquals(2, statistics.getCount());
    assertEquals(150, statistics.getTotal());
    assertEquals(100, statistics.getMax());

  }

  @Test
  public void test() {
    
    ValueCounter counter = new ValueCounter();

    assertEquals(0, counter.getCount());
    assertEquals(0, counter.getTotal());
    assertEquals(Long.MIN_VALUE, counter.getMax());

    counter.add(100);
    assertEquals(1, counter.getCount());
    assertEquals(100, counter.getTotal());
    assertEquals(100, counter.getMax());

    counter.add(50);
    assertEquals(2, counter.getCount());
    assertEquals(150, counter.getTotal());
    assertEquals(100, counter.getMax());

    counter.add(200);
    assertEquals(3, counter.getCount());
    assertEquals(350, counter.getTotal());
    assertEquals(200, counter.getMax());

    counter.add(20);
    assertEquals(4, counter.getCount());
    assertEquals(370, counter.getTotal());
    assertEquals(200, counter.getMax());
    
    counter.reset();
    assertEquals(0, counter.getCount());
    assertEquals(0, counter.getTotal());
    assertEquals(Long.MIN_VALUE, counter.getMax());
    
  }
}
