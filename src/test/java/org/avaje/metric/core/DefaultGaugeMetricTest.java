package org.avaje.metric.core;

import org.avaje.metric.Gauge;
import org.junit.Assert;
import org.junit.Test;

public class DefaultGaugeMetricTest {

  @Test
  public void test() {

    MyGauge myGauge = new MyGauge();
    DefaultGaugeMetric metric = new DefaultGaugeMetric(new DefaultMetricName(MyGauge.class, "test"), myGauge);

    Assert.assertTrue(0d == metric.getValue());
    Assert.assertFalse(metric.collectStatistics());

    myGauge.value = 100d;
    Assert.assertTrue(100d == metric.getValue());
    Assert.assertTrue(metric.collectStatistics());

    DefaultGaugeMetric incrementing = DefaultGaugeMetric.incrementing(new DefaultMetricName(MyGauge.class, "inc"), myGauge);

    myGauge.value = 100d;
    Assert.assertTrue(100d == incrementing.getValue());

    myGauge.value = 150d;
    Assert.assertTrue(50d == incrementing.getValue());

    myGauge.value = 280d;
    Assert.assertTrue(130d == incrementing.getValue());

    myGauge.value = 280d;
    Assert.assertTrue(0d == incrementing.getValue());

  }

  class MyGauge implements Gauge {

    double value;

    @Override
    public double getValue() {
      return value;
    }

  }
}
