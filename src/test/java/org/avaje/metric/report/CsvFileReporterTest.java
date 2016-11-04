package org.avaje.metric.report;

import org.avaje.metric.MetricManager;
import org.avaje.metric.TimedEvent;
import org.avaje.metric.TimedMetric;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Random;

public class CsvFileReporterTest {

  @Test
  public void exercise_with_MetricReportManager() throws InterruptedException, IOException {

    //CsvReportWriter csvReportWriter = new CsvReportWriter(40000);
    //FileReporter fileReporter = new FileReporter(".", "metric-csv-exercise", csvReportWriter);

    MetricReportConfig config = new MetricReportConfig();
    config.setFreqInSeconds(1);
    config.setMetricsFileName("metric-csv-exercise2");
    //config.setThresholdMean(40000);
    //config.setLocalReporter(fileReporter);

    MetricReportManager report = new MetricReportManager(config);

    TimedMetric timedMetric = MetricManager.getTimedMetric("group.type.junk");

    Random random = new Random();
    
    for (int i = 0; i < 50; i++) {
      TimedEvent event = timedMetric.startEvent();
      int plus = random.nextInt(20);
      Thread.sleep(20+plus);
      event.endWithSuccess();
    }
    
    Thread.sleep(2000);

    report.shutdown();
  }
  
}
