package org.avaje.metric.core;

import org.avaje.metric.TimedMetric;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by rob on 4/06/15.
 */
public class NestedContextThreadLocalTest {

  TimedMetric m0 = new DefaultTimedMetric(new DefaultMetricName("org.req","Customer", "getById"));
  TimedMetric m1 = new DefaultTimedMetric(new DefaultMetricName("org.service","CustomerService", "getById"));
  TimedMetric m2 = new DefaultTimedMetric(new DefaultMetricName("org.data","CustomerDAO", "getById"));

  @Test
  public void testNestedSimple() throws Exception {

    NestedContextThreadLocal.start(m0);
    NestedContextThreadLocal.start(m1);
    NestedContextThreadLocal.start(m2);
    NestedContextThreadLocal.end();
    NestedContextThreadLocal.end();
    NestedContextThreadLocal.end();

  }
  @Test
  public void testReport() throws Exception {

    NestedContextThreadLocal.start(m0);
    NestedContextThreadLocal.start(m1);
    NestedContextThreadLocal.end();
    NestedContextThreadLocal.start(m2);
    NestedContextThreadLocal.end();
    NestedContextThreadLocal.end();

  }
}