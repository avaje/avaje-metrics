package io.avaje.metrics.core;


import io.avaje.metrics.report.FileReporter;
import io.avaje.metrics.report.MetricReportConfig;
import io.avaje.metrics.report.MetricReportManager;
import org.testng.annotations.Test;


public class MetricReportManagerTest {

  @Test
  public void test_constructor() throws InterruptedException {

    FileReporter fileReporter = new FileReporter();

    MetricReportConfig config = new MetricReportConfig();
    config.setFreqInSeconds(2);
    config.setReporter(fileReporter);

    MetricReportManager mgr = new MetricReportManager(config);

    Thread.sleep(60*100*5);

    mgr.shutdown();
  }
}
