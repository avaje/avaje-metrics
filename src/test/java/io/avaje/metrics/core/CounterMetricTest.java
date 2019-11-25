package io.avaje.metrics.core;

import io.avaje.metrics.CounterMetric;
import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricManager;
import io.avaje.metrics.statistics.MetricStatistics;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

public class CounterMetricTest {

  @Test
  public void test() {

    CounterMetric counterMetric = MetricManager.getCounterMetric(new DefaultMetricName("org.test.mycountermetric"));

    assertEquals("org.test.mycountermetric", counterMetric.getName().getSimpleName());

    counterMetric.clear();
    assertEquals(0, counterMetric.getCount());

    counterMetric.markEvent();
    assertEquals(1, counterMetric.getCount());
    counterMetric.markEvent();
    assertEquals(2, counterMetric.getCount());
    counterMetric.markEvent();

    assertEquals(3, counterMetric.getCount());
    counterMetric.markEvents(100);
    assertEquals(103, counterMetric.getCount());

    assertThat(collect(counterMetric)).hasSize(1);
    assertEquals(0, counterMetric.getCount());
  }

  private List<MetricStatistics> collect(Metric metric) {
    DStatsCollector collector = new DStatsCollector();
    metric.collect(collector);
    return collector.getList();
  }
}