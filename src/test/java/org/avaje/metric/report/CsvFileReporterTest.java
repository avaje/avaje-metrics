package org.avaje.metric.report;

import org.avaje.metric.MetricManager;
import org.avaje.metric.TimedEvent;
import org.avaje.metric.TimedMetric;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

public class CsvFileReporterTest {

  @Test
  public void exercise_with_MetricReportManager() throws InterruptedException, IOException {
    
    FileReporter fileReporter = new FileReporter(".", "metric-csv-exercise");

    MetricReportConfig config = new MetricReportConfig();
    config.setFreqInSeconds(1);
    config.setLocalReporter(fileReporter);

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
