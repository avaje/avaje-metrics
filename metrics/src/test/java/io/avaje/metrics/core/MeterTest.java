package io.avaje.metrics.core;

import io.avaje.metrics.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MeterTest {

  @Test
  void test() {
    Meter metric = Metrics.meter("org.test.mycounter");
    assertEquals("org.test.mycounter", metric.name());

    metric.reset();
    assertThat(collect(metric)).isEmpty();

    metric.addEvent(1000);
    metric.addEvent(2000);
    metric.addEvent(1500);

    List<Metric.Statistics> stats = collect(metric);
    assertThat(stats).hasSize(1);

    Meter.Stats statistics = (Meter.Stats) stats.get(0);
    assertEquals(3, statistics.count());
    assertEquals(4500, statistics.total());
    assertEquals(2000, statistics.max());
    assertEquals(1500, statistics.mean());
    assertThat(collect(metric)).isEmpty();
  }

  @Test
  void collectCumulative() {
    Meter metric = new DMeter(Metric.ID.of("org.test.mycounter.cumulative"), "");
    metric.addEvent(1000);
    metric.addEvent(2000);
    metric.addEvent(1500);

    Meter.Stats statistics = (Meter.Stats) collect(metric, CollectionMode.CUMULATIVE).get(0);
    assertEquals(3, statistics.count());
    assertEquals(4500, statistics.total());
    assertEquals(2000, statistics.max());
    assertEquals(1500, statistics.mean());

    Meter.Stats statistics2 = (Meter.Stats) collect(metric, CollectionMode.CUMULATIVE).get(0);
    assertEquals(3, statistics2.count());
    assertEquals(4500, statistics2.total());
    assertEquals(0, statistics2.max());
    assertEquals(1500, statistics2.mean());

    metric.addEvent(750);
    Meter.Stats statistics3 = (Meter.Stats) collect(metric, CollectionMode.CUMULATIVE).get(0);
    assertEquals(4, statistics3.count());
    assertEquals(5250, statistics3.total());
    assertEquals(750, statistics3.max());
    assertEquals(1312, statistics3.mean());

    Meter.Stats delta = (Meter.Stats) collect(metric).get(0);
    assertEquals(4, delta.count());
    assertEquals(5250, delta.total());
    assertEquals(0, delta.max());
    assertEquals(0, metric.count());
    assertThat(collect(metric)).isEmpty();
  }

  private List<Metric.Statistics> collect(Metric metric) {
    DStatsCollector collector = new DStatsCollector(NamingMatch.INSTANCE);
    metric.collect(collector);
    return collector.list();
  }

  private List<Metric.Statistics> collect(Metric metric, CollectionMode mode) {
    DStatsCollector collector = new DStatsCollector(NamingMatch.INSTANCE, mode);
    metric.collect(collector);
    return collector.list();
  }
}
