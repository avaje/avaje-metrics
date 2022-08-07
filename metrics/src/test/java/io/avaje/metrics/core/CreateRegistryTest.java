package io.avaje.metrics.core;

import io.avaje.metrics.CounterMetric;
import io.avaje.metrics.MetricManager;
import io.avaje.metrics.MetricRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateRegistryTest {

  @Test
  void createNewRegistry() {

    MetricRegistry registry = MetricManager.createRegistry();

    CounterMetric counterMetric = MetricManager.counter("createNewRegistryCounter");
    CounterMetric counterMetric2 = registry.counter("createNewRegistryCounter");

    assertEquals("createNewRegistryCounter", counterMetric.name());
    assertEquals("createNewRegistryCounter", counterMetric2.name());

    counterMetric.inc();
    assertEquals(1, counterMetric.count());
    assertEquals(0, counterMetric2.count());

    assertThat(counterMetric2).isNotSameAs(counterMetric);
  }
}
