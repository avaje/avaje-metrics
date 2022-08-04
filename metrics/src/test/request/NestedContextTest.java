package io.avaje.metrics.core;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.TimedMetric;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NestedContextTest {

  TimedMetric skipMetric = new DTimedMetric(new DMetricName("org.req.Customer.skipped"));

  TimedMetric m0 = new DTimedMetric(new DMetricName("org.req.Customer.getById"));
  TimedMetric m1 = new DTimedMetric(new DMetricName("org.service.CustomerService.getById"));
  TimedMetric m2 = new DTimedMetric(new DMetricName("org.data.CustomerDAO.getById"));


  @Test
  void testRequestActive() {

    assertFalse(m0.isRequestTiming());
    m0.add(System.nanoTime(), false);

    m0.setRequestTiming(1);
    assertTrue(m0.isRequestTiming());
    assertTrue(m1.isRequestTiming());
    m1.add(System.nanoTime(), true);
    m0.add(System.nanoTime(), true);

    m0.setRequestTiming(0);
    assertFalse(m0.isRequestTiming());
    m0.add(System.nanoTime(), false);
  }

  @Test
  void testNestedSimple() {

    NestedContext context = new NestedContext();
    context.push(m0);
    context.push(m1);
    context.push(m2);
    context.pop();
    context.pop();
    context.pop();
  }

  @Test
  void testReport() {

    NestedContext context = new NestedContext();
    context.push(m0);
    context.pushIfActive(m1);
    context.pop();
    context.pushIfActive(m2);
    context.pop();
    context.pop();

  }

  @Test
  void testSupplier() {
    NestedContext context = new NestedContext();
    context.push(m0);
    context.pushIfActive(() -> new RequestMetric(MetricName.of("sql.select foo")));
    context.pop();
    context.pushIfActive(() -> new RequestMetric(MetricName.of("sql.select bar")));
    context.pop();
    context.pop();
  }

  @Test
  void testSkip() {
    assertFalse(NestedContext.pushIfActive(skipMetric));

    NestedContext.push(m1);
    NestedContext.pop();

    assertFalse(NestedContext.pushIfActive(m2));
  }
}
