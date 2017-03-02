package org.avaje.metric.core;


import org.avaje.metric.GaugeLong;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class DefaultGaugeLongMetricTest {

  @Test
  public void test() {

    MyGauge myGauge = new MyGauge();
    DefaultGaugeLongMetric metric = new DefaultGaugeLongMetric(new DefaultMetricName(MyGauge.class, "test"), myGauge);

    assertEquals(0, metric.getValue());
    assertFalse(metric.collectStatistics());

    myGauge.value = 100;
    assertEquals(100, metric.getValue());
    assertTrue(metric.collectStatistics());

    DefaultGaugeLongMetric incrementing = DefaultGaugeLongMetric.incrementing(new DefaultMetricName(MyGauge.class, "inc"), myGauge);

    myGauge.value = 100;
    //assertFalse(incrementing.collectStatistics());
    assertEquals(100, incrementing.getValue());

    myGauge.value = 150;
    assertTrue(incrementing.collectStatistics());
    assertEquals(50, incrementing.getValue());

    myGauge.value = 280;
    assertTrue(incrementing.collectStatistics());
    assertEquals(130, incrementing.getValue());

    myGauge.value = 280;
    assertFalse(incrementing.collectStatistics());
    assertEquals(0, incrementing.getValue());
  }

  class MyGauge implements GaugeLong {

    long value;

    @Override
    public long getValue() {
      return value;
    }

  }
}
