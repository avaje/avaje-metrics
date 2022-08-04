package io.avaje.metrics.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CounterTest {

  @Test
  void test() {

    Counter counter = new Counter(null);
    assertEquals(0, counter.count());
    assertTrue(counter.isEmpty());

    counter.increment();
    assertFalse(counter.isEmpty());
    assertEquals(1, counter.count());

    counter.increment();
    assertEquals(2, counter.count());
    counter.decrement();
    assertEquals(1, counter.count());
    counter.add(100);
    assertEquals(101, counter.count());
    counter.reset();
    assertTrue(counter.isEmpty());


    counter.add(101);
    assertEquals(101, counter.count());

    counter.reset();
    assertTrue(counter.isEmpty());
    assertEquals(0, counter.count());


    counter.add(101);
    assertEquals(101, counter.count());
    assertFalse(counter.isEmpty());
    assertEquals(101, counter.count());

  }
}
