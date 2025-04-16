package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import io.avaje.metrics.NamingMatch;
import io.avaje.metrics.Tags;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.DoubleSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DGaugeDoubleTest {

  private List<Metric.Statistics> collect(Metric metric) {
    DStatsCollector collector = new DStatsCollector(NamingMatch.INSTANCE);
    metric.collect(collector);
    return collector.list();
  }

  @Test
  void notSkip_when_unchanged() {
    MyGauge myGauge = new MyGauge();
    Metric.ID id = Metric.ID.of(MyGauge.class.getName() + ".test");
    DGaugeDouble metric = new DGaugeDouble(id, myGauge);

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

    Metric.ID id = Metric.ID.of(MyGauge.class.getName() + ".test", Tags.of("k", "v"));
    MyGauge myGauge = new MyGauge();
    DGaugeDouble metric = new DGaugeDouble(id, myGauge);

    assertEquals(0d, metric.value());
    assertThat(collect(metric)).isEmpty();

    myGauge.value = 100d;
    assertEquals(100d, metric.value());
    List<Metric.Statistics> collect = collect(metric);
    assertThat(collect).hasSize(1);
    assertThat(collect.get(0).id()).isEqualTo(id);
  }

  static class MyGauge implements DoubleSupplier {

    double value;

    @Override
    public double getAsDouble() {
      return value;
    }

  }
}
