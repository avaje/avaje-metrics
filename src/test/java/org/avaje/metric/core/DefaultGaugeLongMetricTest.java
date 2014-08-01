package org.avaje.metric.core;



import org.avaje.metric.GaugeLong;
import org.junit.Assert;
import org.junit.Test;

public class DefaultGaugeLongMetricTest {

  @Test
  public void test() {

    MyGauge myGauge = new MyGauge();
    DefaultGaugeLongMetric metric = new DefaultGaugeLongMetric(new DefaultMetricName(MyGauge.class, "test"), myGauge);

    Assert.assertEquals(0, metric.getValue());
    Assert.assertFalse(metric.collectStatistics());

    myGauge.value = 100;
    Assert.assertEquals(100, metric.getValue());
    Assert.assertTrue(metric.collectStatistics());

    DefaultGaugeLongMetric incrementing = DefaultGaugeLongMetric.incrementing(new DefaultMetricName(MyGauge.class, "inc"), myGauge);

    myGauge.value = 100;
    Assert.assertEquals(100, incrementing.getValue());

    myGauge.value = 150;
    Assert.assertEquals(50, incrementing.getValue());

    myGauge.value = 280;
    Assert.assertEquals(130, incrementing.getValue());

    myGauge.value = 280;
    Assert.assertEquals(0, incrementing.getValue());

  }

  class MyGauge implements GaugeLong {

    long value;

    @Override
    public long getValue() {
      return value;
    }

  }
}
