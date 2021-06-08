package io.avaje.metrics.core;

import org.junit.jupiter.api.Test;

class DisableCollectionTest {

  @Test
  void test() {

//    boolean useContext = false;
//
//    System.clearProperty(DefaultMetricManager.METRICS_COLLECTION_DISABLE);
//    DefaultMetricManager mgr = new DefaultMetricManager();
//    Assert.assertFalse(mgr.disable);
//
//
//    System.setProperty(DefaultMetricManager.METRICS_COLLECTION_DISABLE, "true");
//    mgr = new DefaultMetricManager();
//    Assert.assertTrue(mgr.disable);
//
//
//    TimedMetric timedMetric = mgr.getTimedMetric(mgr.name("check.disabled.timed1"));
//    assertEquals(0, timedMetric.getSuccessStatistics(false).getCount());
//    assertEquals(0, timedMetric.getErrorStatistics(false).getCount());
//
//    timedMetric.addEventDuration(false, 1000000);
//    timedMetric.addEventDuration(true, 1000000);
//    timedMetric.addEventSince(true, 1000000);
//    timedMetric.operationEnd(1000000, 100, useContext);
//    timedMetric.operationEnd(1000000, 191, useContext);
//
//    assertEquals(0, timedMetric.getSuccessStatistics(false).getCount());
//    assertEquals(0, timedMetric.getErrorStatistics(false).getCount());
//
//    System.clearProperty(DefaultMetricManager.METRICS_COLLECTION_DISABLE);
  }

  public static void main(String[] args) {

//    System.setProperty("metrics.collection.disable", "true");
//
//    DefaultMetricManager mgr = new DefaultMetricManager();
//    Assert.assertTrue(mgr.disable);
//
//    TimedMetric timedMetric = mgr.getTimedMetric(mgr.name("check.disabled.timed1"));
//    assertEquals(0, timedMetric.getSuccessStatistics(false).getCount());
//    assertEquals(0, timedMetric.getErrorStatistics(false).getCount());
//
//    timedMetric.addEventDuration(true, 1000000);
//    timedMetric.addEventDuration(false, 1000000);
//    assertEquals(0, timedMetric.getSuccessStatistics(false).getCount());
//    assertEquals(0, timedMetric.getErrorStatistics(false).getCount());

  }
}
