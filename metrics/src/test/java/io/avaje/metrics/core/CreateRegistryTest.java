package io.avaje.metrics.core;

import io.avaje.metrics.Counter;
import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateRegistryTest {

  @Test
  void createNewRegistry() {

    MetricRegistry registry = Metrics.createRegistry();
    registry.namingUnderscore();

    Counter counterMetric = Metrics.counter("create.newRegistry.counter");
    Counter counterMetric2 = registry.counter("create.newRegistry.counter");

    assertEquals("create.newRegistry.counter", counterMetric.name());
    assertEquals("create.newRegistry.counter", counterMetric2.name());

    counterMetric.inc();
    counterMetric2.inc();
    counterMetric2.inc();
    assertEquals(1, counterMetric.count());
    assertEquals(2, counterMetric2.count());

    assertThat(counterMetric2).isNotSameAs(counterMetric);

    List<Metric.Statistics> stats = registry.collectMetrics();
    assertThat(stats).hasSize(1);
    assertThat(stats.get(0).name()).isEqualTo("create_newRegistry_counter");
    assertEquals(0, counterMetric2.count());

    counterMetric2.inc();
    counterMetric2.inc();

    List<Metric.Statistics> stats2 = registry.collectMetrics();
    assertThat(stats2).hasSize(1);
    assertThat(stats2.get(0).name()).isEqualTo("create_newRegistry_counter");
  }
}
