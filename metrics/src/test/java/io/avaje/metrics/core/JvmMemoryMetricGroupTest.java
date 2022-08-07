package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JvmMemoryMetricGroupTest {

  @Test
  void testCreateHeapGroup() {

    List<Metric> gaugeMetrics = JvmMemoryMetricGroup.createHeapGroup(true);

    assertEquals(5, gaugeMetrics.size());
    assertEquals("jvm.memory.heap.init", gaugeMetrics.get(0).name());
    assertEquals("jvm.memory.heap.used", gaugeMetrics.get(1).name());
    assertEquals("jvm.memory.heap.committed", gaugeMetrics.get(2).name());
    assertEquals("jvm.memory.heap.max", gaugeMetrics.get(3).name());
    assertEquals("jvm.memory.heap.pct", gaugeMetrics.get(4).name());

    assertEquals("jvm.memory.heap.init", gaugeMetrics.get(0).name());
  }

  @Test
  void testCreateNonHeapGroup() {

    List<Metric> gaugeMetrics = JvmMemoryMetricGroup.createNonHeapGroup(true);

    assertThat(gaugeMetrics.size()).isGreaterThan(1);
    assertEquals("jvm.memory.nonheap.init", gaugeMetrics.get(0).name());
    assertEquals("jvm.memory.nonheap.used", gaugeMetrics.get(1).name());
    assertEquals("jvm.memory.nonheap.committed", gaugeMetrics.get(2).name());

    assertEquals("jvm.memory.nonheap.init", gaugeMetrics.get(0).name());
  }

}
