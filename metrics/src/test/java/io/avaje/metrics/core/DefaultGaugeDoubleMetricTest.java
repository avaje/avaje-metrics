package io.avaje.metrics.core;

import io.avaje.metrics.GaugeDouble;
import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricStats;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultGaugeDoubleMetricTest {

  private List<MetricStats> collect(Metric metric) {
    DStatsCollector collector = new DStatsCollector();
    metric.collect(collector);
    return collector.getList();
  }

  @Test
  void skipCollection_when_unchanged() {

    MyGauge myGauge = new MyGauge();
    DefaultGaugeDoubleMetric metric = new DefaultGaugeDoubleMetric(new DefaultMetricName(DefaultGaugeLongMetricTest.MyGauge.class, "test"), myGauge);

    assertEquals(0.0, metric.value());
    assertThat(collect(metric)).isEmpty();

    myGauge.value = 100.0;
    assertEquals(100.0, metric.value());
    assertThat(collect(metric)).hasSize(1);
    assertEquals(100.0, metric.value());

    // skip
    assertThat(collect(metric)).isEmpty();
    assertEquals(100.0, metric.value());

    myGauge.value = 110.0;

    assertThat(collect(metric)).hasSize(1);
    assertEquals(110.0, metric.value());

    // skip
    assertThat(collect(metric)).isEmpty();

    myGauge.value = 90.0;
    assertThat(collect(metric)).hasSize(1);
    assertEquals(90.0, metric.value());

  }

  @Test
  void test() {

    MyGauge myGauge = new MyGauge();
    DefaultGaugeDoubleMetric metric = new DefaultGaugeDoubleMetric(new DefaultMetricName(MyGauge.class, "test"), myGauge);

    assertTrue(0d == metric.value());
    assertThat(collect(metric)).isEmpty();

    myGauge.value = 100d;
    assertTrue(100d == metric.value());
    assertThat(collect(metric)).hasSize(1);

    DefaultGaugeDoubleMetric incrementing = DefaultGaugeDoubleMetric.incrementing(new DefaultMetricName(MyGauge.class, "inc"), myGauge);

    myGauge.value = 100d;
    assertTrue(100d == incrementing.value());

    myGauge.value = 150d;
    assertTrue(50d == incrementing.value());

    myGauge.value = 280d;
    assertTrue(130d == incrementing.value());

    myGauge.value = 280d;
    assertTrue(0d == incrementing.value());

  }

  static class MyGauge implements GaugeDouble {

    double value;

    @Override
    public double value() {
      return value;
    }

  }
}
