package io.avaje.metrics.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValueCounterTest {

  @Test
  void testGetStatisticsWithNoReset() {
    ValueCounter counter = new ValueCounter("junk");
    assertEquals(Long.MIN_VALUE, counter.getMax());

    counter.add(100);

    assertEquals(1, counter.getCount());
    assertEquals(100, counter.getTotal());
    assertEquals(100, counter.getMax());

    counter.add(50);
    // no activity, just get statistics again
    assertEquals(2, counter.getCount());
    assertEquals(150, counter.getTotal());
    assertEquals(100, counter.getMax());
  }

  @Test
  void test() {
    ValueCounter counter = new ValueCounter("junk");

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
