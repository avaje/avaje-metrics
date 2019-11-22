package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricManager;
import io.avaje.metrics.ValueMetric;
import io.avaje.metrics.statistics.MetricStatistics;
import io.avaje.metrics.statistics.ValueStatistics;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

public class ValueMetricTest {

  @Test
  public void test() {

    ValueMetric metric = MetricManager.getValueMetric(new DefaultMetricName("org.test.mycounter"));

    assertEquals("org.test.mycounter", metric.getName().getSimpleName());

    metric.clear();
    assertThat(collect(metric)).isEmpty();

    metric.addEvent(1000);
    metric.addEvent(2000);
    metric.addEvent(1500);

    List<MetricStatistics> stats = collect(metric);
    assertThat(stats).hasSize(1);

    ValueStatistics statistics = (ValueStatistics)stats.get(0);
    assertEquals(3, statistics.getCount());
    assertEquals(4500, statistics.getTotal());
    assertEquals(2000, statistics.getMax());
    assertEquals(1500, statistics.getMean());


    assertThat(collect(metric)).isEmpty();
  }

  private List<MetricStatistics> collect(Metric metric) {
    DStatsCollector collector = new DStatsCollector();
    metric.collect(collector);
    return collector.getList();
  }
}
