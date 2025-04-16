package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValueCounterTest {

  @Test
  void testGetStatisticsWithNoReset() {
    ValueCounter counter = new ValueCounter(Metric.ID.of("junk"));
    assertEquals(Long.MIN_VALUE, counter.max());

    counter.add(100);

    assertEquals(1, counter.count());
    assertEquals(100, counter.total());
    assertEquals(100, counter.max());

    counter.add(50);
    // no activity, just get statistics again
    assertEquals(2, counter.count());
    assertEquals(150, counter.total());
    assertEquals(100, counter.max());
  }

  @Test
  void test() {
    ValueCounter counter = new ValueCounter(Metric.ID.of("junk"));

    assertEquals(0, counter.count());
    assertEquals(0, counter.total());
    assertEquals(Long.MIN_VALUE, counter.max());

    counter.add(100);
    assertEquals(1, counter.count());
    assertEquals(100, counter.total());
    assertEquals(100, counter.max());

    counter.add(50);
    assertEquals(2, counter.count());
    assertEquals(150, counter.total());
    assertEquals(100, counter.max());

    counter.add(200);
    assertEquals(3, counter.count());
    assertEquals(350, counter.total());
    assertEquals(200, counter.max());

    counter.add(20);
    assertEquals(4, counter.count());
    assertEquals(370, counter.total());
    assertEquals(200, counter.max());

    counter.reset();
    assertEquals(0, counter.count());
    assertEquals(0, counter.total());
    assertEquals(Long.MIN_VALUE, counter.max());
  }
}
