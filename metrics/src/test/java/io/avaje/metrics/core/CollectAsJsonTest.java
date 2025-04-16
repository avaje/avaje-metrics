package io.avaje.metrics.core;

import io.avaje.metrics.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CollectAsJsonTest {

  private final MetricRegistry registry = Metrics.createRegistry();

  @Test
  void collectAsJson() {

    Tags tags = Tags.of("a", "b");
    long startNanos = System.nanoTime();
    Timer timer = registry.timer("my.timer");
    Timer timer2 = registry.timer("my.timer", tags);
    Timer timer3 = registry.timer("myBucket", tags, 400, 900);

    Counter counter = registry.counter("my.count");
    Counter counter2 = registry.counter("my.count", tags);
    counter.inc();
    counter.inc();

    counter2.inc();

    Meter meter = registry.meter("my.meter");
    Meter meter2 = registry.meter("my.meter", tags);
    meter.addEvent(42);
    meter.addEvent(44);
    meter.addEvent(46);
    meter2.addEvent(993);

    timer.add(startNanos);
    timer2.add(startNanos);
    timer3.add(startNanos);

    registry.gauge("my.gauge0", () -> 142D);
    registry.gauge("my.gauge1", () -> 200L);
    registry.gauge("my.gauge0", tags, () -> 442D);
    registry.gauge("my.gauge1", tags, () -> 400L);

    timer.add(startNanos);

    String asJson = registry.collectAsJson().asJson();
    assertThat(asJson).contains("{\"name\":\"my.count\",\"value\":2}");
    assertThat(asJson).contains("{\"name\":\"my.count\",\"value\":1,\"tags\":[\"a\",\"b\"]}");
    assertThat(asJson).contains("{\"name\":\"my.timer\",\"count\":2,\"mean\":");
    assertThat(asJson).contains("{\"name\":\"my.timer\",\"count\":1,\"mean\":"); // with tags
    assertThat(asJson).contains("{\"name\":\"myBucket\",\"count\":1,\"mean\":"); // with tags
    assertThat(asJson).contains("{\"name\":\"my.meter\",\"count\":3,\"mean\":44,\"max\":46,\"total\":132}");
    assertThat(asJson).contains("{\"name\":\"my.meter\",\"count\":1,\"mean\":993,\"max\":993,\"total\":993,\"tags\":[\"a\",\"b\"]}");
    assertThat(asJson).contains("{\"name\":\"my.gauge0\",\"value\":142.0}");
    assertThat(asJson).contains("{\"name\":\"my.gauge0\",\"value\":442.0,\"tags\":[\"a\",\"b\"]}");
    assertThat(asJson).contains("{\"name\":\"my.gauge1\",\"value\":200}");
    assertThat(asJson).contains("{\"name\":\"my.gauge1\",\"value\":400,\"tags\":[\"a\",\"b\"]}");
  }
}
