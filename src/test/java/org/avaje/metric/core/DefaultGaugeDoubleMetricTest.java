package org.avaje.metric.core;

import org.avaje.metric.GaugeDouble;
import org.avaje.metric.Metric;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

public class DefaultGaugeDoubleMetricTest {


  private List<Metric> collect(Metric metric) {
    List<Metric> list = new ArrayList<>();
    metric.collectStatistics(list);
    return list;
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
