package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricStats;
import io.avaje.metrics.NamingMatch;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.DoubleSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultGaugeDoubleMetricTest {

  private List<MetricStats> collect(Metric metric) {
    DStatsCollector collector = new DStatsCollector(NamingMatch.INSTANCE);
    metric.collect(collector);
    return collector.list();
  }

  @Test
  void notSkip_when_unchanged() {
    MyGauge myGauge = new MyGauge();
    DGaugeDouble metric = new DGaugeDouble(DefaultGaugeLongMetricTest.MyGauge.class.getName() + ".test", myGauge);

    assertEquals(0.0, metric.value());
    assertThat(collect(metric)).isEmpty();

    myGauge.value = 100.0;
    assertEquals(100.0, metric.value());
    assertThat(collect(metric)).hasSize(1);
    assertEquals(100.0, metric.value());

    // skip
    assertThat(collect(metric)).hasSize(1);
    assertEquals(100.0, metric.value());

    myGauge.value = 110.0;

    assertThat(collect(metric)).hasSize(1);
    assertEquals(110.0, metric.value());

    // skip
    assertThat(collect(metric)).hasSize(1);

    myGauge.value = 90.0;
    assertThat(collect(metric)).hasSize(1);
    assertEquals(90.0, metric.value());
  }

  @Test
  void test() {

    MyGauge myGauge = new MyGauge();
    DGaugeDouble metric = new DGaugeDouble(MyGauge.class.getName() + ".test", myGauge);

    assertEquals(0d, metric.value());
    assertThat(collect(metric)).isEmpty();

    myGauge.value = 100d;
    assertEquals(100d, metric.value());
    assertThat(collect(metric)).hasSize(1);
  }

  static class MyGauge implements DoubleSupplier {

    double value;

    @Override
    public double getAsDouble() {
      return value;
    }

  }
}
