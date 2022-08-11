package io.avaje.metrics.report;

import io.avaje.metrics.Metrics;
import io.avaje.metrics.Timer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

class CsvFileReporterTest {

  @Test
  void exercise_with_MetricReportManager() throws InterruptedException, IOException {

    //CsvReportWriter csvReportWriter = new CsvReportWriter(40000);
    //FileReporter fileReporter = new FileReporter(".", "metric-csv-exercise", csvReportWriter);

    MetricReportConfig config = new MetricReportConfig();
    config.setFreqInSeconds(1);
    config.setMetricsFileName("metric-csv-exercise2");
    //config.setThresholdMean(40000);
    //config.setLocalReporter(fileReporter);

    MetricReportManager report = new MetricReportManager(config);

    Timer timedMetric = Metrics.timer("group.type.junk");

    Random random = new Random();

    for (int i = 0; i < 50; i++) {
      Timer.Event event = timedMetric.startEvent();
      int plus = random.nextInt(20);
      Thread.sleep(20 + plus);
      event.end();

      if (i == 20) {
        Runtime.getRuntime().gc();
      }
    }

    Thread.sleep(2000);

    Runtime.getRuntime().gc();

    Thread.sleep(2000);

    report.shutdown();
  }

}
