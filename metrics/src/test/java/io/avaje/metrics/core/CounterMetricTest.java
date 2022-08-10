package io.avaje.metrics.core;

import io.avaje.metrics.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CounterMetricTest {

  @Test
  void test() {

    Counter counterMetric = Metrics.counter("org.test.mycountermetric");
    assertEquals("org.test.mycountermetric", counterMetric.name());

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

  private List<Metric.Statistics> collect(Metric metric) {
    DStatsCollector collector = new DStatsCollector(NamingMatch.INSTANCE);
    metric.collect(collector);
    return collector.list();
  }
}
