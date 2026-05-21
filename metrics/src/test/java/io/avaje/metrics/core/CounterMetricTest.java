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

  @Test
  void tags() {
    MetricRegistry registry = Metrics.registry();
    Counter counter0 = registry.counter("one", Tags.of("k", "v"));
    Counter counter1 = registry.counter("one", Tags.of("k", "x"));
    Counter counter2 = registry.counter("one");

    assertThat(counter0).isNotSameAs(counter1);
    assertThat(counter0).isNotSameAs(counter2);

    counter0.inc();
    counter1.inc();
    counter1.inc();
    counter2.inc();
    counter2.inc();
    counter2.inc();

    List<Metric.Statistics> s0 = collect(counter0);
    assertThat(s0).hasSize(1);
    assertThat(s0.get(0).id().tags()).isEqualTo(Tags.of("k", "v"));
    assertThat(counter0.count()).isEqualTo(0);
    assertThat(counter1.count()).isEqualTo(2);
    assertThat(counter2.count()).isEqualTo(3);

    List<Metric.Statistics> s1 = collect(counter1);
    assertThat(s1).hasSize(1);
    assertThat(s1.get(0).id().tags()).isEqualTo(Tags.of("k", "x"));
    assertThat(counter0.count()).isEqualTo(0);
    assertThat(counter1.count()).isEqualTo(0);
    assertThat(counter2.count()).isEqualTo(3);

    List<Metric.Statistics> s2 = collect(counter2);
    assertThat(s2).hasSize(1);
    assertThat(s2.get(0).id().tags()).isEqualTo(Tags.of());
    assertThat(counter0.count()).isEqualTo(0);
    assertThat(counter1.count()).isEqualTo(0);
    assertThat(counter2.count()).isEqualTo(0);
  }

  @Test
  void collectCumulative() {
    DCounter counterMetric = new DCounter(Metric.ID.of("org.test.mycountermetric.cumulative"));
    counterMetric.inc();
    counterMetric.inc();

    List<Metric.Statistics> stats = collect(counterMetric, CollectionMode.CUMULATIVE);
    assertThat(stats).hasSize(1);
    assertThat(((Counter.Stats) stats.get(0)).count()).isEqualTo(2);
    assertThat(counterMetric.count()).isEqualTo(2);

    List<Metric.Statistics> stats2 = collect(counterMetric, CollectionMode.CUMULATIVE);
    assertThat(stats2).hasSize(1);
    assertThat(((Counter.Stats) stats2.get(0)).count()).isEqualTo(2);
    assertThat(counterMetric.count()).isEqualTo(2);

    List<Metric.Statistics> stats3 = collect(counterMetric);
    assertThat(stats3).hasSize(1);
    assertThat(((Counter.Stats) stats3.get(0)).count()).isEqualTo(2);
    assertThat(counterMetric.count()).isEqualTo(0);
  }

  private List<Metric.Statistics> collect(Metric metric) {
    DStatsCollector collector = new DStatsCollector(NamingMatch.INSTANCE);
    metric.collect(collector);
    return collector.list();
  }

  private List<Metric.Statistics> collect(Metric metric, CollectionMode mode) {
    DStatsCollector collector = new DStatsCollector(NamingMatch.INSTANCE, mode);
    metric.collect(collector);
    return collector.list();
  }
}
