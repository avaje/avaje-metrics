package io.avaje.metrics.core;


import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.Metric;
import io.avaje.metrics.statistics.MetricStatistics;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultGaugeLongMetricTest {

  @Test
  public void skipCollection_when_unchanged() {

    MyGauge myGauge = new MyGauge();
    DefaultGaugeLongMetric metric = new DefaultGaugeLongMetric(new DefaultMetricName(MyGauge.class, "test"), myGauge);

    assertEquals(0, metric.getValue());
    assertThat(collect(metric)).isEmpty();

    myGauge.value = 100;
    assertEquals(100, metric.getValue());
    assertThat(collect(metric)).hasSize(1);

    assertEquals(100, metric.getValue());

    // skip
    assertThat(collect(metric)).isEmpty();
    assertEquals(100, metric.getValue());

    myGauge.value = 110;

    assertThat(collect(metric)).hasSize(1);
    assertEquals(110, metric.getValue());

    // skip
    assertThat(collect(metric)).isEmpty();

    myGauge.value = 90;
    assertThat(collect(metric)).hasSize(1);
    assertEquals(90, metric.getValue());

  }

  private List<MetricStatistics> collect(Metric metric) {
    DStatsCollector collector = new DStatsCollector();
    metric.collect(collector);
    return collector.getList();
  }

  @Test
  public void test() {

    MyGauge myGauge = new MyGauge();
    DefaultGaugeLongMetric metric = new DefaultGaugeLongMetric(new DefaultMetricName(MyGauge.class, "test"), myGauge);

    assertEquals(0, metric.getValue());
    assertThat(collect(metric)).isEmpty();

    myGauge.value = 100;
    assertEquals(100, metric.getValue());
    assertThat(collect(metric)).hasSize(1);

    DefaultGaugeLongMetric incrementing = DefaultGaugeLongMetric.incrementing(new DefaultMetricName(MyGauge.class, "inc"), myGauge);

    myGauge.value = 100;
    //assertFalse(incrementing.collectStatistics());
    assertEquals(100, incrementing.getValue());

    myGauge.value = 150;
    assertThat(collect(metric)).hasSize(1);
    assertEquals(50, incrementing.getValue());

    myGauge.value = 280;
    assertThat(collect(metric)).hasSize(1);
    assertEquals(130, incrementing.getValue());

    myGauge.value = 280;
    assertThat(collect(metric)).isEmpty();
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
