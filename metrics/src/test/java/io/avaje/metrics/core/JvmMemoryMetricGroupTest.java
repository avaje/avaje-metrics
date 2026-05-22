package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import io.avaje.metrics.Tags;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JvmMemoryMetricGroupTest {

  @Test
  void testCreateHeapGroup() {

    DefaultMetricProvider registry = new DefaultMetricProvider();
    JvmMemory.createHeapGroup(registry, true, true, Tags.EMPTY);

    List<Metric> gaugeMetrics = new ArrayList<>(registry.metrics());

    assertEquals(5, gaugeMetrics.size());
    assertThat(gaugeMetrics).extracting(Metric::name).contains(
      "jvm.memory.heap.init"
      , "jvm.memory.heap.used"
      , "jvm.memory.heap.committed"
      , "jvm.memory.heap.max"
      , "jvm.memory.heap.pct");
    assertThat(gaugeMetrics).filteredOn(metric -> !"jvm.memory.heap.pct".equals(metric.name()))
      .extracting(Metric::unit)
      .containsOnly("MiBy");
    assertThat(gaugeMetrics).filteredOn(metric -> "jvm.memory.heap.pct".equals(metric.name()))
      .extracting(Metric::unit)
      .containsOnly("%");
  }

  @Test
  void testCreateNonHeapGroup() {

    DefaultMetricProvider registry = new DefaultMetricProvider();
    JvmMemory.createNonHeapGroup(registry, true, true, Tags.EMPTY);
    List<Metric> gaugeMetrics = new ArrayList<>(registry.metrics());

    assertThat(gaugeMetrics.size()).isGreaterThan(1);

    assertThat(gaugeMetrics).extracting(Metric::name).contains(
      "jvm.memory.nonheap.init"
      , "jvm.memory.nonheap.used"
      , "jvm.memory.nonheap.committed");
    assertThat(gaugeMetrics).filteredOn(metric -> Set.of(
        "jvm.memory.nonheap.init",
        "jvm.memory.nonheap.used",
        "jvm.memory.nonheap.committed",
        "jvm.memory.nonheap.max").contains(metric.name()))
      .extracting(Metric::unit)
      .containsOnly("MiBy");
    assertThat(gaugeMetrics).filteredOn(metric -> metric.name().equals("jvm.memory.nonheap.pct"))
      .allSatisfy(metric -> assertThat(metric.unit()).isEqualTo("%"));

  }

}
