package org.avaje.metric.core;

import org.avaje.metric.TimedMetric;
import org.junit.Test;

/**
 * Created by rob on 4/06/15.
 */
public class NestedContextTest {

  TimedMetric m0 = new DefaultTimedMetric(new DefaultMetricName("org.req","Customer", "getById"));
  TimedMetric m1 = new DefaultTimedMetric(new DefaultMetricName("org.service","CustomerService", "getById"));
  TimedMetric m2 = new DefaultTimedMetric(new DefaultMetricName("org.data","CustomerDAO", "getById"));

  @Test
  public void testNestedSimple() throws Exception {

    NestedContext context = new NestedContext();
    context.start(m0);
    context.start(m1);
    context.start(m2);
    context.end();
    context.end();
    context.end();

  }

  @Test
  public void testReport() throws Exception {

    NestedContext context = new NestedContext();
    context.start(m0);
    context.start(m1);
    context.end();
    context.start(m2);
    context.end();
    context.end();

  }
}