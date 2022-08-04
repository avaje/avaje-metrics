package io.avaje.metrics.core;

import io.avaje.metrics.CounterMetric;
import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricManager;
import io.avaje.metrics.MetricStats;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CounterMetricTest {

  @Test
  void test() {

    CounterMetric counterMetric = MetricManager.counter(new DefaultMetricName("org.test.mycountermetric"));

    assertEquals("org.test.mycountermetric", counterMetric.name().simpleName());

    counterMetric.reset();
    assertEquals(0, counterMetric.count());

    counterMetric.inc();
    assertEquals(1, counterMetric.count());
    counterMetric.inc();
    assertEquals(2, counterMetric.count());
    counterMetric.inc();

    assertEquals(3, counterMetric.count());
    counterMetric.inc(100);
    assertEquals(103, counterMetric.count());

    assertThat(collect(counterMetric)).hasSize(1);
    assertEquals(0, counterMetric.count());
  }

  private List<MetricStats> collect(Metric metric) {
    DStatsCollector collector = new DStatsCollector();
    metric.collect(collector);
    return collector.getList();
  }
}
