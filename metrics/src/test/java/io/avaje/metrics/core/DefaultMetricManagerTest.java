package io.avaje.metrics.core;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.TimingMetricInfo;
import orange.truck.Customer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultMetricManagerTest {

//  @Test
//  void testGetRequestTimingMetrics() {
//
//    DefaultMetricManager mgr = new DefaultMetricManager();
//
//    TimedMetric m0 = mgr.timed(MetricName.of("org.req.Customer.m0"));
//    TimedMetric m1 = mgr.timed(MetricName.of("org.req.Customer.m1"));
//    TimedMetric m2 = mgr.timed(MetricName.of("org.req.Customer.m2"), 100, 200);
//
//    List<TimingMetricInfo> timingMetrics = mgr.getRequestTimingMetrics(null);
//    assertEquals(0, timingMetrics.size());
//
//    m0.setRequestTiming(1);
//    timingMetrics = mgr.getRequestTimingMetrics(null);
//    assertEquals(1, timingMetrics.size());
//    assertEquals(m0.name().simpleName(), timingMetrics.get(0).getName());
//
//
//    m2.setRequestTiming(10);
//    timingMetrics = mgr.getRequestTimingMetrics(null);
//    assertEquals(2, timingMetrics.size());
//
//    m1.setRequestTiming(10);
//    timingMetrics = mgr.getRequestTimingMetrics(null);
//    assertEquals(3, timingMetrics.size());
//
//    m0.decrementRequestTiming();
//    timingMetrics = mgr.getRequestTimingMetrics(null);
//    assertEquals(2, timingMetrics.size());
//
//  }

  @Test
  void testSetCollection() {

    DefaultMetricManager mgr = new DefaultMetricManager();

    TimedMetric m0 = mgr.timed(MetricName.of(Customer.class, "doSomething"));

    TimedMetric m0b = mgr.timed(MetricName.of("orange.truck.Customer.doSomething"));

    assertSame(m0, m0b);
    //assertEquals("na.Customer.doSomething", m0.getName().getSimpleName());

//    TimedMetric m1 = mgr.timed(MetricName.of("org.req.Customer.m1"));
//    TimedMetric m2 = mgr.timed(MetricName.of("org.req.Customer.m2"), 100, 200);
//
//    List<TimingMetricInfo> allTimingMetrics = mgr.getAllTimingMetrics(null);
//    assertEquals(3, allTimingMetrics.size());
//
//    List<TimingMetricInfo> requestTimingMetrics = mgr.getRequestTimingMetrics(null);
//    assertEquals(0, requestTimingMetrics.size());
//
//    assertEquals(0, m0.getRequestTiming());
//
//    assertTrue(mgr.setRequestTimingCollection(Customer.class, "doSomething", 1));
//    assertEquals(1, m0.getRequestTiming());
//
//    requestTimingMetrics = mgr.getRequestTimingMetrics(null);
//    assertEquals(1, requestTimingMetrics.size());
//
//    List<TimingMetricInfo> changes = mgr.setRequestTimingCollectionUsingMatch("org.req*", 3);
//    assertEquals(2, changes.size());
//    assertEquals("org.req.Customer.m1", changes.get(0).getName());
//    assertEquals("org.req.Customer.m2", changes.get(1).getName());
//
//    requestTimingMetrics = mgr.getRequestTimingMetrics(null);
//    assertEquals(3, requestTimingMetrics.size());
//
//
//    assertFalse(mgr.setRequestTimingCollection(Customer.class, "methodDoesNotExist", 1));

  }
}
