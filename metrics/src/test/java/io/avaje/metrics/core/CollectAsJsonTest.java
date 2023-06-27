package io.avaje.metrics.core;

import io.avaje.metrics.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CollectAsJsonTest {

  private final MetricRegistry registry = Metrics.createRegistry();

  @Test
  void collectAsJson() {

    long startNanos = System.nanoTime();
    Timer timer = registry.timed("my.timer");
    Counter counter = registry.counter("my.count");
    counter.inc();
    counter.inc();

    Meter meter = registry.meter("my.meter");
    meter.addEvent(42);
    meter.addEvent(44);
    meter.addEvent(46);

    timer.add(startNanos);

    registry.gauge("my.gauge0", () -> 142D);
    registry.gauge("my.gauge1", () -> 200L);

    timer.add(startNanos);

    String asJson = registry.collectAsJson().asJson();
    assertThat(asJson).contains("{\"name\":\"my.count\",\"value\":2}");
    assertThat(asJson).contains("{\"name\":\"my.timer\",\"count\":2,\"mean\":");
    assertThat(asJson).contains("{\"name\":\"my.meter\",\"count\":3,\"mean\":44,\"max\":46,\"total\":132}");
    assertThat(asJson).contains("{\"name\":\"my.gauge0\",\"value\":142.0}");
    assertThat(asJson).contains("{\"name\":\"my.gauge1\",\"value\":200}");
  }
}
