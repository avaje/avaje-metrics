package org.avaje.metric.core;

import org.avaje.metric.CounterStatistics;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class CounterTest {

  @Test
  public void test() {
    
    Counter counter = new Counter();
    assertEquals(0, counter.getCount());
    assertTrue(counter.isEmpty());

    counter.increment();
    assertFalse(counter.isEmpty());
    assertEquals(1, counter.getCount());
    
    counter.increment();
    assertEquals(2, counter.getCount());
    counter.decrement();
    assertEquals(1, counter.getCount());
    counter.add(100);
    assertEquals(101, counter.getCount());
    
    CounterStatistics stats0 = counter.collectStatistics();
    assertEquals(101, stats0.getCount());
    assertTrue(counter.isEmpty());

    
    counter.add(101);
    assertEquals(101, counter.getCount());

    CounterStatistics stats1 = counter.getStatistics(true);
    assertEquals(101, stats1.getCount());
    
    assertTrue(counter.isEmpty());
    assertEquals(0, counter.getCount());
    

    counter.add(101);
    assertEquals(101, counter.getCount());

    CounterStatistics stats2 = counter.getStatistics(false);
    assertEquals(101, stats2.getCount());
    assertFalse(counter.isEmpty());
    assertEquals(101, counter.getCount());

  }
}
