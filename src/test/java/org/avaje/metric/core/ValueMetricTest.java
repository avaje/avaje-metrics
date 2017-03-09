package org.avaje.metric.core;

import org.avaje.metric.Metric;
import org.avaje.metric.MetricManager;
import org.avaje.metric.ValueMetric;
import org.avaje.metric.ValueStatistics;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

public class ValueMetricTest {

  @Test
  public void test() {

    ValueMetric metric = MetricManager.getValueMetric(new DefaultMetricName("org", "test", "mycounter"));

    assertEquals("org", metric.getName().getGroup());
    assertEquals("test", metric.getName().getType());
    assertEquals("mycounter", metric.getName().getName());

    metric.clearStatistics();
    assertThat(collect(metric)).isEmpty();

    metric.addEvent(1000);
    metric.addEvent(2000);
    metric.addEvent(1500);

    assertThat(collect(metric)).hasSize(1);
    
    ValueStatistics statistics = metric.getCollectedStatistics();
    
    assertEquals(3, statistics.getCount());
    assertEquals(4500, statistics.getTotal());
    assertEquals(2000, statistics.getMax());
    assertEquals(1500, statistics.getMean());


    assertThat(collect(metric)).isEmpty();
  }

  private List<Metric> collect(Metric metric) {
    List<Metric> list = new ArrayList<>();
    metric.collectStatistics(list);
    return list;
  }
}
