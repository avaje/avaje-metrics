package org.avaje.metric.core;


import org.avaje.metric.report.FileReporter;
import org.avaje.metric.report.MetricReportManager;
import org.junit.Test;


public class MetricReportManagerTest {

  @Test
  public void test_constructor() throws InterruptedException {

    FileReporter fileReporter = new FileReporter();
    MetricReportManager mgr = new MetricReportManager(20, fileReporter);

    Thread.sleep(60*1000*5);

    mgr.shutdown();
  }
}
