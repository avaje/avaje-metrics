package org.avaje.metric.report;

import java.io.IOException;
import java.util.Random;

import org.avaje.metric.MetricManager;
import org.avaje.metric.TimedEvent;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.core.DefaultMetricName;
import org.avaje.metric.filereport.FileReporter;
import org.avaje.metric.report.MetricReportManager;
import org.junit.Test;

public class FileReporterTest {

  @Test
  public void testMe() throws InterruptedException, IOException {
    
    FileReporter fileReporter = new FileReporter(".", "metric-test");
    MetricReportManager report = new MetricReportManager(2, fileReporter, null);
    report.toString();
    
    TimedMetric timedMetric = MetricManager.getTimedMetric(new DefaultMetricName("group","type","junk"));
    
    
    Random random = new Random();
    
    for (int i = 0; i < 100; i++) {
      TimedEvent startEvent = timedMetric.startEvent();   
      int plus = random.nextInt(100);
      Thread.sleep(100+plus);
      startEvent.endWithSuccess();
    }
    
    Thread.sleep(4000);
    //report.writeAll();
    
  }
  
}
