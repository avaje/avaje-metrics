package io.avaje.metrics.core;

import io.avaje.metrics.Timer;
import orange.truck.Customer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class DefaultMetricManagerTest {

  @Test
  void testSetCollection() {

    DefaultMetricProvider mgr = new DefaultMetricProvider();

    Timer m0 = mgr.timed(Customer.class.getName() + ".doSomething");
    Timer m0b = mgr.timed("orange.truck.Customer.doSomething");

    assertSame(m0, m0b);
  }
}
