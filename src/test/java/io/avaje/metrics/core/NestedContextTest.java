package io.avaje.metrics.core;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.TimedMetric;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class NestedContextTest {


  TimedMetric skipMetric = new DefaultTimedMetric(new DefaultMetricName("org.req.Customer.skipped"));

  TimedMetric m0 = new DefaultTimedMetric(new DefaultMetricName("org.req.Customer.getById"));
  TimedMetric m1 = new DefaultTimedMetric(new DefaultMetricName("org.service.CustomerService.getById"));
  TimedMetric m2 = new DefaultTimedMetric(new DefaultMetricName("org.data.CustomerDAO.getById"));


  @Test
  public void testRequestActive() throws Exception {

    assertFalse(m0.isActiveThreadContext());
    m0.operationEnd(System.nanoTime(), false);

    m0.setRequestTimingCollection(1);
    assertTrue(m0.isActiveThreadContext());
    assertTrue(m1.isActiveThreadContext());
    m1.operationEnd(System.nanoTime(), true);
    m0.operationEnd(System.nanoTime(), true);

    m0.setRequestTimingCollection(0);
    assertFalse(m0.isActiveThreadContext());
    m0.operationEnd(System.nanoTime(), false);
  }

  @Test
  public void testNestedSimple() throws Exception {

    NestedContext context = new NestedContext();
    context.push(m0);
    context.push(m1);
    context.push(m2);
    context.pop();
    context.pop();
    context.pop();
  }

  @Test
  public void testReport() throws Exception {

    NestedContext context = new NestedContext();
    context.push(m0);
    context.pushIfActive(m1);
    context.pop();
    context.pushIfActive(m2);
    context.pop();
    context.pop();

  }

  @Test
  public void testSupplier() throws Exception {

    NestedContext context = new NestedContext();
    context.push(m0);
    context.pushIfActive(() -> new RequestMetric(MetricName.of("sql.select foo")));
    context.pop();
    context.pushIfActive(() -> new RequestMetric(MetricName.of("sql.select bar")));
    context.pop();
    context.pop();

  }


  @Test
  public void testSkip() throws Exception {

    assertFalse(NestedContext.pushIfActive(skipMetric));

    NestedContext.push(m1);
    NestedContext.pop();

    assertFalse(NestedContext.pushIfActive(m2));
  }
}
