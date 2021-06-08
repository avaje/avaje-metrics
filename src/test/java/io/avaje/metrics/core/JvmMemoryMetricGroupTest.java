package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JvmMemoryMetricGroupTest {

  @Test
  public void testCreateHeapGroup() {

    List<Metric> gaugeMetrics = JvmMemoryMetricGroup.createHeapGroup(true);

    assertEquals(5, gaugeMetrics.size());
    assertEquals("jvm.memory.heap.init", gaugeMetrics.get(0).getName().getSimpleName());
    assertEquals("jvm.memory.heap.used", gaugeMetrics.get(1).getName().getSimpleName());
    assertEquals("jvm.memory.heap.committed", gaugeMetrics.get(2).getName().getSimpleName());
    assertEquals("jvm.memory.heap.max", gaugeMetrics.get(3).getName().getSimpleName());
    assertEquals("jvm.memory.heap.pct", gaugeMetrics.get(4).getName().getSimpleName());

    assertEquals("jvm.memory.heap.init", gaugeMetrics.get(0).getName().getSimpleName());
  }

  @Test
  public void testCreateNonHeapGroup() {

    List<Metric> gaugeMetrics = JvmMemoryMetricGroup.createNonHeapGroup(true);

    assertThat(gaugeMetrics.size()).isGreaterThan(1);
    assertEquals("jvm.memory.nonheap.init", gaugeMetrics.get(0).getName().getSimpleName());
    assertEquals("jvm.memory.nonheap.used", gaugeMetrics.get(1).getName().getSimpleName());
    assertEquals("jvm.memory.nonheap.committed", gaugeMetrics.get(2).getName().getSimpleName());

    assertEquals("jvm.memory.nonheap.init", gaugeMetrics.get(0).getName().getSimpleName());
  }

}
