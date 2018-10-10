package org.avaje.metric.core;

import org.avaje.metric.Metric;
import org.avaje.metric.MetricManager;
import org.avaje.metric.statistics.MetricStatistics;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertTrue;

public class JvmProcessMemoryTest {

  @Test
  public void testGetMetrics() throws Exception {

    List<Metric> metrics = JvmProcessMemory.createGauges();
    dump(metrics);
  }

  private void dump(List<Metric> metrics) throws InterruptedException {
    for (int i = 0; i < 5; i++) {
      if (!metrics.isEmpty()) {
        Thread.sleep(1000);

        List<MetricStatistics> list = MetricManager.collectNonEmptyJvmMetrics();

        String values = list.toString();
        System.out.println(values);
        if (i == 0) {
          // will be empty when values not changing, always have values
          // on first run though so assert then
          assertTrue(values.contains("jvm.memory.process.vmhwm"));
          assertTrue(values.contains("jvm.memory.process.vmrss"));
        }
      }
    }
  }

}
