package io.avaje.metrics.core;

import io.avaje.metrics.GaugeDouble;
import io.avaje.metrics.Metric;
import io.avaje.metrics.statistics.MetricStatistics;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

public class DefaultGaugeDoubleMetricTest {


  private List<MetricStatistics> collect(Metric metric) {
    DStatsCollector collector = new DStatsCollector();
    metric.collect(collector);
    return collector.getList();
  }

  @Test
  public void skipCollection_when_unchanged() {

    MyGauge myGauge = new MyGauge();
    DefaultGaugeDoubleMetric metric = new DefaultGaugeDoubleMetric(new DefaultMetricName(DefaultGaugeLongMetricTest.MyGauge.class, "test"), myGauge);

    assertEquals(0.0, metric.getValue());
    assertThat(collect(metric)).isEmpty();

    myGauge.value = 100.0;
    assertEquals(100.0, metric.getValue());
    assertThat(collect(metric)).hasSize(1);
    assertEquals(100.0, metric.getValue());

    // skip
    assertThat(collect(metric)).isEmpty();
    assertEquals(100.0, metric.getValue());

    myGauge.value = 110.0;

    assertThat(collect(metric)).hasSize(1);
    assertEquals(110.0, metric.getValue());

    // skip
    assertThat(collect(metric)).isEmpty();

    myGauge.value = 90.0;
    assertThat(collect(metric)).hasSize(1);
    assertEquals(90.0, metric.getValue());

  }
  @Test
  public void test() {

    MyGauge myGauge = new MyGauge();
    DefaultGaugeDoubleMetric metric = new DefaultGaugeDoubleMetric(new DefaultMetricName(MyGauge.class, "test"), myGauge);

    Assert.assertTrue(0d == metric.getValue());
    assertThat(collect(metric)).isEmpty();

    myGauge.value = 100d;
    Assert.assertTrue(100d == metric.getValue());
    assertThat(collect(metric)).hasSize(1);

    DefaultGaugeDoubleMetric incrementing = DefaultGaugeDoubleMetric.incrementing(new DefaultMetricName(MyGauge.class, "inc"), myGauge);

    myGauge.value = 100d;
    Assert.assertTrue(100d == incrementing.getValue());

    myGauge.value = 150d;
    Assert.assertTrue(50d == incrementing.getValue());

    myGauge.value = 280d;
    Assert.assertTrue(130d == incrementing.getValue());

    myGauge.value = 280d;
    Assert.assertTrue(0d == incrementing.getValue());

  }

  class MyGauge implements GaugeDouble {

    double value;

    @Override
    public double getValue() {
      return value;
    }

  }
}
