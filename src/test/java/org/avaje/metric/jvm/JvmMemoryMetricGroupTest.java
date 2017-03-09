package org.avaje.metric.jvm;

import org.avaje.metric.GaugeDoubleMetric;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

public class JvmMemoryMetricGroupTest {

  @Test
  public void testCreateHeapGroup() throws Exception {

    GaugeDoubleMetric[] gaugeMetrics = JvmMemoryMetricGroup.createHeapGroup();

    assertEquals(3, gaugeMetrics.length);
    //assertEquals("init", gaugeMetrics[0].getName().getName());
    assertEquals("used", gaugeMetrics[0].getName().getName());
    assertEquals("committed", gaugeMetrics[1].getName().getName());
    //assertEquals("max", gaugeMetrics[3].getName().getName());
    assertEquals("pct", gaugeMetrics[2].getName().getName());

    assertEquals("jvm.memory.heap.used", gaugeMetrics[0].getName().getSimpleName());

  }

  @Test
  public void testCreateNonHeapGroup() throws Exception {

    GaugeDoubleMetric[] gaugeMetrics = JvmMemoryMetricGroup.createNonHeapGroup();

    assertThat(gaugeMetrics.length).isGreaterThan(1);
    //assertEquals("init", gaugeMetrics[0].getName().getName());
    assertEquals("used", gaugeMetrics[0].getName().getName());
    assertEquals("committed", gaugeMetrics[1].getName().getName());

    assertEquals("jvm.memory.nonheap.used", gaugeMetrics[0].getName().getSimpleName());

  }
}