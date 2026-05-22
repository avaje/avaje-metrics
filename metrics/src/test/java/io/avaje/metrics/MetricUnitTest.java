package io.avaje.metrics;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetricUnitTest {

  @Test
  void configuredUnits_arePropagatedToMetricsAndStats() {
    var registry = Metrics.createRegistry();
    var counter = registry.counterBuilder("app.rows.read").unit("row").build();
    var meter = registry.meterBuilder("app.bytes.sent").unit("By").build();
    var gaugeLong = registry.gauge("jvm.memory.used").unit("MiBy").ofLongs(() -> 42L);
    var gaugeDouble = registry.gauge("app.cpu.utilization").unit("%").ofDoubles(() -> 75.5d);

    counter.inc(3);
    meter.addEvent(1024);

    var stats = byName(registry.collectMetrics(CollectionMode.CUMULATIVE));

    assertThat(counter.unit()).isEqualTo("row");
    assertThat(((Counter.Stats) stats.get("app.rows.read")).unit()).isEqualTo("row");
    assertThat(meter.unit()).isEqualTo("By");
    assertThat(((Meter.Stats) stats.get("app.bytes.sent")).unit()).isEqualTo("By");
    assertThat(gaugeLong.unit()).isEqualTo("MiBy");
    assertThat(((GaugeLong.Stats) stats.get("jvm.memory.used")).unit()).isEqualTo("MiBy");
    assertThat(gaugeDouble.unit()).isEqualTo("%");
    assertThat(((GaugeDouble.Stats) stats.get("app.cpu.utilization")).unit()).isEqualTo("%");
  }

  @Test
  void defaultUnits_areApplied() {
    var registry = Metrics.createRegistry();
    var counter = registry.counter("app.events");
    var meter = registry.meter("app.payload");
    var gauge = registry.gauge("jvm.threads.active", () -> 7L);
    var timer = registry.timer("app.latency");

    counter.inc();
    meter.addEvent(10);
    timer.addEventDuration(true, 1_000_000L);

    var stats = byName(registry.collectMetrics(CollectionMode.CUMULATIVE));

    assertThat(counter.unit()).isEqualTo("{event}");
    assertThat(((Counter.Stats) stats.get("app.events")).unit()).isEqualTo("{event}");
    assertThat(meter.unit()).isEmpty();
    assertThat(((Meter.Stats) stats.get("app.payload")).unit()).isEmpty();
    assertThat(gauge.unit()).isEmpty();
    assertThat(((GaugeLong.Stats) stats.get("jvm.threads.active")).unit()).isEmpty();
    assertThat(timer.unit()).isEqualTo("us");
    assertThat(((Timer.Stats) stats.get("app.latency")).unit()).isEqualTo("us");
  }

  @Test
  void blankUnits_normalizeToEmpty() {
    var registry = Metrics.createRegistry();
    var meter = registry.meterBuilder("app.blank.unit").unit("   ").build();

    meter.addEvent(1);

    var stat = (Meter.Stats) registry.collectMetrics(CollectionMode.CUMULATIVE).get(0);
    assertThat(meter.unit()).isEmpty();
    assertThat(stat.unit()).isEmpty();
  }

  @Test
  void conflictingUnits_areRejected() {
    var registry = Metrics.createRegistry();
    registry.counterBuilder("app.counter.conflict").unit("row").build();
    registry.gauge("app.gauge.conflict").unit("MiBy").ofLongs(() -> 42L);

    assertThatThrownBy(() -> registry.counterBuilder("app.counter.conflict").unit("request").build())
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("app.counter.conflict");
    assertThatThrownBy(() -> registry.gauge("app.gauge.conflict").unit("By").ofLongs(() -> 24L))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("app.gauge.conflict");
  }

  private static Map<String, Metric.Statistics> byName(Iterable<Metric.Statistics> stats) {
    return toStream(stats).collect(Collectors.toMap(Metric.Statistics::name, Function.identity()));
  }

  private static java.util.stream.Stream<Metric.Statistics> toStream(Iterable<Metric.Statistics> stats) {
    return java.util.stream.StreamSupport.stream(stats.spliterator(), false);
  }
}
