package org.avaje.metric.core;

import org.avaje.metric.CounterMetric;
import org.avaje.metric.CounterStatistics;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricManager;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

public class CounterMetricTest {

  @Test
  public void test() {

    CounterMetric counterMetric = MetricManager.getCounterMetric(new DefaultMetricName("org", "test", "mycountermetric"));

    assertEquals("org", counterMetric.getName().getGroup());
    assertEquals("test", counterMetric.getName().getType());
    assertEquals("mycountermetric", counterMetric.getName().getName());

    counterMetric.clearStatistics();
    assertEquals(0, counterMetric.getStatistics(false).getCount());

    counterMetric.markEvent();
    assertEquals(1, counterMetric.getStatistics(false).getCount());
    counterMetric.markEvent();
    assertEquals(2, counterMetric.getStatistics(false).getCount());
    counterMetric.markEvent();

    assertEquals(3, counterMetric.getStatistics(false).getCount());
    counterMetric.markEvents(100);
    assertEquals(103, counterMetric.getStatistics(false).getCount());

    assertThat(collect(counterMetric)).hasSize(1);
    CounterStatistics collectedStatistics = counterMetric.getCollectedStatistics();
    assertEquals(103, collectedStatistics.getCount());


    assertEquals(0, counterMetric.getStatistics(false).getCount());

  }

  private List<Metric> collect(Metric metric) {
    List<Metric> list = new ArrayList<>();
    metric.collectStatistics(list);
    return list;
  }
}
