package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import io.avaje.metrics.Metrics;
import io.avaje.metrics.Meter;
import io.avaje.metrics.MetricStats;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ValueMetricTest {

  @Test
  void test() {
    Meter metric = Metrics.meter("org.test.mycounter");
    assertEquals("org.test.mycounter", metric.name());

    metric.reset();
    assertThat(collect(metric)).isEmpty();

    metric.addEvent(1000);
    metric.addEvent(2000);
    metric.addEvent(1500);

    List<MetricStats> stats = collect(metric);
    assertThat(stats).hasSize(1);

    Meter.Stats statistics = (Meter.Stats) stats.get(0);
    assertEquals(3, statistics.count());
    assertEquals(4500, statistics.total());
    assertEquals(2000, statistics.max());
    assertEquals(1500, statistics.mean());
    assertThat(collect(metric)).isEmpty();
  }

  private List<MetricStats> collect(Metric metric) {
    DStatsCollector collector = new DStatsCollector();
    metric.collect(collector);
    return collector.list();
  }
}
