package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricStats;
import io.avaje.metrics.NamingMatch;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.LongSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultGaugeLongMetricTest {

  @Test
  void skipCollection_when_unchanged() {

    MyGauge myGauge = new MyGauge();
    DGaugeLong metric = new DGaugeLong(MyGauge.class.getName() + ".test", myGauge);

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

  private List<MetricStats> collect(Metric metric) {
    DStatsCollector collector = new DStatsCollector(NamingMatch.INSTANCE);
    metric.collect(collector);
    return collector.list();
  }

  @Test
  void test() {

    MyGauge myGauge = new MyGauge();
    DGaugeLong metric = new DGaugeLong(MyGauge.class.getName() + ".test", myGauge);

    assertEquals(0, metric.value());
    assertThat(collect(metric)).isEmpty();

    myGauge.value = 100;
    assertEquals(100, metric.value());
    assertThat(collect(metric)).hasSize(1);

    DGaugeLong incrementing = DGaugeLong.incrementing(MyGauge.class.getName() + ".inc", myGauge);

    myGauge.value = 100;
    //assertFalse(incrementing.collectStatistics());
    assertEquals(100, incrementing.value());

    myGauge.value = 150;
    assertThat(collect(metric)).hasSize(1);
    assertEquals(50, incrementing.value());

    myGauge.value = 280;
    assertThat(collect(metric)).hasSize(1);
    assertEquals(130, incrementing.value());

    myGauge.value = 280;
    assertThat(collect(metric)).isEmpty();
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
