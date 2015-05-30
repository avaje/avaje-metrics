package org.avaje.metric.jvm;

import org.avaje.metric.GaugeDoubleGroup;
import org.avaje.metric.GaugeDoubleMetric;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JvmMemoryMetricGroupTest {

  @Test
  public void testCreateHeapGroup() throws Exception {

    GaugeDoubleGroup heapGroup = JvmMemoryMetricGroup.createHeapGroup();
    GaugeDoubleMetric[] gaugeMetrics = heapGroup.getGaugeMetrics();

    assertEquals(5, gaugeMetrics.length);
    assertEquals("init", gaugeMetrics[0].getName().getName());
    assertEquals("used", gaugeMetrics[1].getName().getName());
    assertEquals("committed", gaugeMetrics[2].getName().getName());
    assertEquals("max", gaugeMetrics[3].getName().getName());
    assertEquals("pct", gaugeMetrics[4].getName().getName());

    assertEquals("jvm.memory.heap.init", gaugeMetrics[0].getName().getSimpleName());

  }

  @Test
  public void testCreateNonHeapGroup() throws Exception {

    GaugeDoubleGroup heapGroup = JvmMemoryMetricGroup.createNonHeapGroup();
    GaugeDoubleMetric[] gaugeMetrics = heapGroup.getGaugeMetrics();

    assertTrue("At least 3", gaugeMetrics.length >= 3);
    assertEquals("init", gaugeMetrics[0].getName().getName());
    assertEquals("used", gaugeMetrics[1].getName().getName());
    assertEquals("committed", gaugeMetrics[2].getName().getName());

    assertEquals("jvm.memory.nonheap.init", gaugeMetrics[0].getName().getSimpleName());

  }
}