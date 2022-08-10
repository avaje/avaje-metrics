package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricStats;
import io.avaje.metrics.NamingMatch;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.LongSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DGaugeLongTest {

  @Test
  void skipCollection_when_unchanged() {

    MyGauge myGauge = new MyGauge();
    DGaugeLong metric = DGaugeLong.of(MyGauge.class.getName() + ".test", myGauge, true);

    assertEquals(0, metric.value());
    assertThat(collect(metric)).isEmpty();

    myGauge.value = 100;
    assertEquals(100, metric.value());
    assertThat(collect(metric)).hasSize(1);

    assertEquals(100, metric.value());

    // skip
    assertThat(collect(metric)).isEmpty();
    assertEquals(100, metric.value());

    myGauge.value = 110;

    assertThat(collect(metric)).hasSize(1);
    assertEquals(110, metric.value());

    // skip
    assertThat(collect(metric)).isEmpty();

    myGauge.value = 90;
    assertThat(collect(metric)).hasSize(1);
    assertEquals(90, metric.value());

  }

  private GaugeLong.Stats collectGauge(Metric metric) {
    List<MetricStats> collect = collect(metric);
    return collect.isEmpty() ? null : (GaugeLong.Stats)collect.get(0);
  }

  private List<MetricStats> collect(Metric metric) {
    DStatsCollector collector = new DStatsCollector(NamingMatch.INSTANCE);
    metric.collect(collector);
    return collector.list();
  }

  @Test
  void test_incrementing() {
    MyGauge myGauge = new MyGauge();
    DGaugeLong metric = DGaugeLong.of(MyGauge.class.getName() + ".test", myGauge, true);

    assertEquals(0, metric.value());
    assertThat(collectGauge(metric)).isNull();

    myGauge.value = 100;
    assertEquals(100, metric.value());
    assertThat(collectGauge(metric).value()).isEqualTo(100);

    DGaugeLong incrementing = DGaugeLong.of(MyGauge.class.getName() + ".inc", GaugeLong.incrementing(myGauge), true);

    myGauge.value = 100;
    assertEquals(100, incrementing.value());

    myGauge.value = 150;
    assertThat(collectGauge(incrementing).value()).isEqualTo(50);

    myGauge.value = 280;
    assertThat(collectGauge(incrementing).value()).isEqualTo(130);

    myGauge.value = 280;
    assertThat(collectGauge(incrementing)).isNull();
    assertEquals(0, incrementing.value());
  }

  static class MyGauge implements LongSupplier {

    long value;

    @Override
    public long getAsLong() {
      return value;
    }

  }
}
