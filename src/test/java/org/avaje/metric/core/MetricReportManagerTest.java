package org.avaje.metric.core;


import org.avaje.metric.report.MetricReportManager;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MetricReportManagerTest {

  @Test
  public void test_constructor() throws InterruptedException {

    ScheduledExecutorService executorService  = Executors.newSingleThreadScheduledExecutor();

    MetricReportManager mgr = new MetricReportManager(executorService, 2, null);
    mgr.hashCode();

    Thread.sleep(8500);

  }
}
