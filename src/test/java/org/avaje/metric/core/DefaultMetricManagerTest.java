package org.avaje.metric.core;

import org.avaje.metric.AbstractTimedMetric;
import org.avaje.metric.BucketTimedMetric;
import org.avaje.metric.TimedMetric;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class DefaultMetricManagerTest {

  @Test
  public void testGetRequestTimingMetrics() throws Exception {

    DefaultMetricManager mgr = new DefaultMetricManager();

    TimedMetric m0 = mgr.getTimedMetric(new DefaultMetricName("org.req", "Customer", "m0"));
    TimedMetric m1 = mgr.getTimedMetric(new DefaultMetricName("org.req", "Customer", "m1"));
    BucketTimedMetric m2 = mgr.getBucketTimedMetric(new DefaultMetricName("org.req", "Customer", "m2"), 100, 200);

    List<AbstractTimedMetric> timingMetrics = mgr.getRequestTimingMetrics();
    assertEquals(0, timingMetrics.size());

    m0.setRequestTimingCollection(1);
    timingMetrics = mgr.getRequestTimingMetrics();
    assertEquals(1, timingMetrics.size());
    assertEquals(m0.getName(), timingMetrics.get(0).getName());


    m2.setRequestTimingCollection(10);
    timingMetrics = mgr.getRequestTimingMetrics();
    assertEquals(2, timingMetrics.size());

    m1.setRequestTimingCollection(10);
    timingMetrics = mgr.getRequestTimingMetrics();
    assertEquals(3, timingMetrics.size());

    m0.decrementCollectionCount();
    timingMetrics = mgr.getRequestTimingMetrics();
    assertEquals(2, timingMetrics.size());

  }
}