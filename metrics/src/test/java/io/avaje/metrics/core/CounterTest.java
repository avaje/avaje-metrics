package io.avaje.metrics.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CounterTest {

  @Test
  void test() {
    DCounter counter = new DCounter(null);
    assertEquals(0, counter.count());

    counter.inc();
    assertEquals(1, counter.count());

    counter.inc();
    assertEquals(2, counter.count());
    counter.dec();
    assertEquals(1, counter.count());
    counter.inc(100);
    assertEquals(101, counter.count());
    counter.dec(40);
    assertEquals(61, counter.count());
    counter.reset();

    counter.inc(101);
    assertEquals(101, counter.count());

    counter.reset();
    assertEquals(0, counter.count());


    counter.inc(101);
    assertEquals(101, counter.count());
    assertEquals(101, counter.count());
  }
}
