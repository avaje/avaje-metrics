package org.avaje.metric.core;

import org.assertj.core.api.AbstractByteAssert;
import org.avaje.metric.Metric;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

public class JvmMemoryMetricGroupTest {

  @Test
  public void testCreateHeapGroup() {

    List<Metric> gaugeMetrics = JvmMemoryMetricGroup.createHeapGroup();

    assertEquals(5, gaugeMetrics.size());
    assertEquals("init", gaugeMetrics.get(0).getName().getName());
    assertEquals("used", gaugeMetrics.get(1).getName().getName());
    assertEquals("committed", gaugeMetrics.get(2).getName().getName());
    assertEquals("max", gaugeMetrics.get(3).getName().getName());
    assertEquals("pct", gaugeMetrics.get(4).getName().getName());

    assertEquals("jvm.memory.heap.init", gaugeMetrics.get(0).getName().getSimpleName());
  }

  @Test
  public void testCreateNonHeapGroup() {

    List<Metric> gaugeMetrics = JvmMemoryMetricGroup.createNonHeapGroup();

    assertThat(gaugeMetrics.size()).isGreaterThan(1);
    assertEquals("init", gaugeMetrics.get(0).getName().getName());
    assertEquals("used", gaugeMetrics.get(1).getName().getName());
    assertEquals("committed", gaugeMetrics.get(2).getName().getName());

    assertEquals("jvm.memory.nonheap.init", gaugeMetrics.get(0).getName().getSimpleName());
  }

}
