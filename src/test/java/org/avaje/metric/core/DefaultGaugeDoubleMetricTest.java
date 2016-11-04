package org.avaje.metric.core;

import org.avaje.metric.GaugeDouble;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DefaultGaugeDoubleMetricTest {

  @Test
  public void test() {

    MyGauge myGauge = new MyGauge();
    DefaultGaugeDoubleMetric metric = new DefaultGaugeDoubleMetric(new DefaultMetricName(MyGauge.class, "test"), myGauge);

    Assert.assertTrue(0d == metric.getValue());
    Assert.assertFalse(metric.collectStatistics());

    myGauge.value = 100d;
    Assert.assertTrue(100d == metric.getValue());
    Assert.assertTrue(metric.collectStatistics());

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
