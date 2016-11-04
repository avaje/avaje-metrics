package org.avaje.metric.core;


import org.avaje.metric.report.FileReporter;
import org.avaje.metric.report.MetricReportConfig;
import org.avaje.metric.report.MetricReportManager;
import org.testng.annotations.Test;


public class MetricReportManagerTest {

  @Test
  public void test_constructor() throws InterruptedException {

    FileReporter fileReporter = new FileReporter();

    MetricReportConfig config = new MetricReportConfig();
    config.setFreqInSeconds(2);
    config.setLocalReporter(fileReporter);

    MetricReportManager mgr = new MetricReportManager(config);

    Thread.sleep(60*100*5);

    mgr.shutdown();
  }
}
