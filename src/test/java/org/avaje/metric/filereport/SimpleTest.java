package org.avaje.metric.filereport;

import java.io.IOException;
import java.util.Random;

import org.avaje.metric.MetricManager;
import org.avaje.metric.MetricName;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.TimedMetricEvent;
import org.avaje.metric.report.MetricReportManager;
import org.junit.Test;

public class SimpleTest {

  @Test
  public void testMe() throws InterruptedException, IOException {
    
    FileReporter fileReporter = new FileReporter(".", "metric-test");
    MetricReportManager report = new MetricReportManager(10, fileReporter, null);
    report.toString();
    
    TimedMetric timedMetric = MetricManager.getTimedMetric(new MetricName("group","type","junk"));
    
    
    Random random = new Random();
    
    for (int i = 0; i < 100; i++) {
      TimedMetricEvent startEvent = timedMetric.startEvent();   
      int plus = random.nextInt(100);
      Thread.sleep(100+plus);
      startEvent.endWithSuccess();
    }
    
    Thread.sleep(8000);
    //report.writeAll();
    
    Thread.sleep(30000);
    
  }
  
}
