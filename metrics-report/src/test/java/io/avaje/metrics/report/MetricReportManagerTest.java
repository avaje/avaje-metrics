package io.avaje.metrics.report;


import io.avaje.metrics.report.FileReporter;
import io.avaje.metrics.report.MetricReportConfig;
import io.avaje.metrics.report.MetricReportManager;
import org.junit.jupiter.api.Test;

class MetricReportManagerTest {

  @Test
  void test_constructor() throws InterruptedException {

    FileReporter fileReporter = new FileReporter();

    MetricReportConfig config = new MetricReportConfig();
    config.setFreqInSeconds(2);
    config.setReporter(fileReporter);

    MetricReportManager mgr = new MetricReportManager(config);

    Thread.sleep(60 * 100 * 5);

    mgr.shutdown();
  }
}
