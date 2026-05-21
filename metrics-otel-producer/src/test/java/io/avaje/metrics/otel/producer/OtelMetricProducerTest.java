package io.avaje.metrics.otel.producer;

import io.avaje.metrics.Counter;
import io.avaje.metrics.Meter;
import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.Metrics;
import io.avaje.metrics.Tags;
import io.avaje.metrics.Timer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class OtelMetricProducerTest {

  @Test
  void counter_cumulativeAcrossCollections() {
    var epochNanosSource = new MutableEpochNanosSource(epochNanos(Instant.parse("2026-01-01T00:00:00Z")));
    var registry = Metrics.createRegistry();
    var producer = new DOtelMetricProducer(registry, InstrumentationScopeInfo.create("test.scope"), 0, epochNanosSource);
    var counter = registry.counter("app.requests");

    counter.inc(5);
    epochNanosSource.advanceSeconds(10);
    var first = onlyMetric(producer.produce(Resource.empty()));

    counter.inc(3);
    epochNanosSource.advanceSeconds(10);
    var second = onlyMetric(producer.produce(Resource.empty()));

    epochNanosSource.advanceSeconds(10);
    var third = onlyMetric(producer.produce(Resource.empty()));

    assertThat(first.getName()).isEqualTo("app.requests");
    assertThat(first.getLongSumData().getAggregationTemporality()).isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(first.getLongSumData().isMonotonic()).isTrue();
    assertThat(onlyLongPoint(first).getStartEpochNanos()).isEqualTo(epochNanos(Instant.parse("2026-01-01T00:00:00Z")));
    assertThat(onlyLongPoint(first).getEpochNanos()).isEqualTo(epochNanos(Instant.parse("2026-01-01T00:00:10Z")));
    assertThat(onlyLongPoint(first).getValue()).isEqualTo(5);

    assertThat(second.getName()).isEqualTo("app.requests");
    assertThat(onlyLongPoint(second).getStartEpochNanos()).isEqualTo(epochNanos(Instant.parse("2026-01-01T00:00:00Z")));
    assertThat(onlyLongPoint(second).getEpochNanos()).isEqualTo(epochNanos(Instant.parse("2026-01-01T00:00:20Z")));
    assertThat(onlyLongPoint(second).getValue()).isEqualTo(8);

    assertThat(third.getName()).isEqualTo("app.requests");
    assertThat(onlyLongPoint(third).getStartEpochNanos()).isEqualTo(epochNanos(Instant.parse("2026-01-01T00:00:00Z")));
    assertThat(onlyLongPoint(third).getEpochNanos()).isEqualTo(epochNanos(Instant.parse("2026-01-01T00:00:30Z")));
    assertThat(onlyLongPoint(third).getValue()).isEqualTo(8);
  }

  @Test
  void counter_tagsConvertedAndNullEntriesSkipped() {
    var epochNanosSource = new MutableEpochNanosSource(epochNanos(Instant.parse("2026-01-01T00:00:00Z")));
    var registry = Metrics.createRegistry();
    var producer = new DOtelMetricProducer(registry, InstrumentationScopeInfo.create("test.scope"), 0, epochNanosSource);
    Counter counter = registry.counter("app.login.count", Tags.of(null, "env:prod", "region:us-east"));

    counter.inc(10);
    epochNanosSource.advanceSeconds(5);
    MetricData metric = onlyMetric(producer.produce(Resource.empty()));
    LongPointData point = onlyLongPoint(metric);

    assertThat(point.getAttributes().get(AttributeKey.stringKey("env"))).isEqualTo("prod");
    assertThat(point.getAttributes().get(AttributeKey.stringKey("region"))).isEqualTo("us-east");
    assertThat(point.getAttributes().size()).isEqualTo(2);
  }

  @Test
  void timer_metricsAreCumulativeAndMaxResets() {
    var epochNanosSource = new MutableEpochNanosSource(epochNanos(Instant.parse("2026-01-01T00:00:00Z")));
    var registry = Metrics.createRegistry();
    var producer = new DOtelMetricProducer(registry, InstrumentationScopeInfo.create("test.scope"), 0, epochNanosSource);
    Timer timer = registry.timer("app.service.method");

    timer.addEventDuration(true, 5_000_000L);
    timer.addEventDuration(false, 2_000_000L);
    epochNanosSource.advanceSeconds(5);

    Map<String, MetricData> first = byName(producer.produce(Resource.empty()));

    assertThat(first).containsKeys(
      "app.service.method.count",
      "app.service.method.total",
      "app.service.method.max",
      "app.service.method.error.count",
      "app.service.method.error.total",
      "app.service.method.error.max");

    assertThat(first.get("app.service.method.count").getLongSumData().getAggregationTemporality())
      .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(onlyLongSumPoint(first.get("app.service.method.count")).getStartEpochNanos())
      .isEqualTo(epochNanos(Instant.parse("2026-01-01T00:00:00Z")));
    assertThat(onlyLongSumPoint(first.get("app.service.method.count")).getEpochNanos())
      .isEqualTo(epochNanos(Instant.parse("2026-01-01T00:00:05Z")));
    assertThat(onlyLongSumPoint(first.get("app.service.method.count")).getValue()).isEqualTo(1);
    assertThat(onlyLongSumPoint(first.get("app.service.method.total")).getValue()).isEqualTo(5_000L);
    assertThat(onlyLongGaugePoint(first.get("app.service.method.max")).getValue()).isEqualTo(5_000L);
    assertThat(onlyLongSumPoint(first.get("app.service.method.error.count")).getValue()).isEqualTo(1);
    assertThat(onlyLongSumPoint(first.get("app.service.method.error.total")).getValue()).isEqualTo(2_000L);
    assertThat(onlyLongGaugePoint(first.get("app.service.method.error.max")).getValue()).isEqualTo(2_000L);

    epochNanosSource.advanceSeconds(5);
    Map<String, MetricData> second = byName(producer.produce(Resource.empty()));

    assertThat(onlyLongSumPoint(second.get("app.service.method.count")).getValue()).isEqualTo(1);
    assertThat(onlyLongSumPoint(second.get("app.service.method.total")).getValue()).isEqualTo(5_000L);
    assertThat(onlyLongGaugePoint(second.get("app.service.method.max")).getValue()).isEqualTo(0L);
    assertThat(onlyLongSumPoint(second.get("app.service.method.error.count")).getValue()).isEqualTo(1);
    assertThat(onlyLongSumPoint(second.get("app.service.method.error.total")).getValue()).isEqualTo(2_000L);
    assertThat(onlyLongGaugePoint(second.get("app.service.method.error.max")).getValue()).isEqualTo(0L);

    timer.addEventDuration(true, 3_000_000L);
    timer.addEventDuration(false, 7_000_000L);
    epochNanosSource.advanceSeconds(5);
    Map<String, MetricData> third = byName(producer.produce(Resource.empty()));

    assertThat(onlyLongSumPoint(third.get("app.service.method.count")).getValue()).isEqualTo(2);
    assertThat(onlyLongSumPoint(third.get("app.service.method.total")).getValue()).isEqualTo(8_000L);
    assertThat(onlyLongGaugePoint(third.get("app.service.method.max")).getValue()).isEqualTo(3_000L);
    assertThat(onlyLongSumPoint(third.get("app.service.method.error.count")).getValue()).isEqualTo(2);
    assertThat(onlyLongSumPoint(third.get("app.service.method.error.total")).getValue()).isEqualTo(9_000L);
    assertThat(onlyLongGaugePoint(third.get("app.service.method.error.max")).getValue()).isEqualTo(7_000L);
  }

  @Test
  void timedThreshold_appliesToCumulativeTotal() {
    var epochNanosSource = new MutableEpochNanosSource(epochNanos(Instant.parse("2026-01-01T00:00:00Z")));
    var registry = Metrics.createRegistry();
    var producer = new DOtelMetricProducer(registry, InstrumentationScopeInfo.create("test.scope"), 10_000, epochNanosSource);
    Timer timer = registry.timer("app.fast.method");

    timer.addEventDuration(true, 1_000_000L);
    epochNanosSource.advanceSeconds(5);

    assertThat(producer.produce(Resource.empty())).isEmpty();

    timer.addEventDuration(true, 9_000_000L);
    epochNanosSource.advanceSeconds(5);

    Map<String, MetricData> metrics = byName(producer.produce(Resource.empty()));

    assertThat(onlyLongSumPoint(metrics.get("app.fast.method.count")).getValue()).isEqualTo(2);
    assertThat(onlyLongSumPoint(metrics.get("app.fast.method.total")).getValue()).isEqualTo(10_000L);
    assertThat(onlyLongGaugePoint(metrics.get("app.fast.method.max")).getValue()).isEqualTo(9_000L);
  }

  @Test
  void meterAndGauges_areMapped() {
    var epochNanosSource = new MutableEpochNanosSource(epochNanos(Instant.parse("2026-01-01T00:00:00Z")));
    var registry = Metrics.createRegistry();
    var producer = new DOtelMetricProducer(registry, InstrumentationScopeInfo.create("test.scope"), 0, epochNanosSource);

    Meter meter = registry.meter("app.bytes.sent");
    meter.addEvent(1024);
    meter.addEvent(2048);
    registry.gauge("jvm.threads.active", () -> 42L);
    registry.gauge("jvm.memory.pct", () -> 0.75);
    epochNanosSource.advanceSeconds(5);

    Map<String, MetricData> metrics = byName(producer.produce(Resource.empty()));

    assertThat(metrics.get("app.bytes.sent.count").getLongSumData().getAggregationTemporality())
      .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(onlyLongSumPoint(metrics.get("app.bytes.sent.count")).getValue()).isEqualTo(2);
    assertThat(onlyLongSumPoint(metrics.get("app.bytes.sent.total")).getValue()).isEqualTo(3072L);
    assertThat(onlyLongGaugePoint(metrics.get("app.bytes.sent.max")).getValue()).isEqualTo(2048L);
    assertThat(onlyLongGaugePoint(metrics.get("jvm.threads.active")).getValue()).isEqualTo(42L);
    assertThat(metrics.get("jvm.memory.pct").getDoubleGaugeData().getPoints().iterator().next().getValue()).isEqualTo(0.75);
  }

  @Test
  void registersWithSdkMeterProvider() {
    MetricRegistry registry = Metrics.createRegistry();
    Counter counter = registry.counter("app.checkout");
    counter.inc(4);

    InMemoryMetricReader reader = InMemoryMetricReader.create();
    try (SdkMeterProvider meterProvider = SdkMeterProvider.builder()
      .setResource(Resource.create(Attributes.of(AttributeKey.stringKey("service.name"), "catalog")))
      .registerMetricReader(reader)
      .registerMetricProducer(OtelMetricProducer.builder()
        .registry(registry)
        .scopeName("custom.scope")
        .build())
      .build()) {

      MetricData metric = onlyMetric(reader.collectAllMetrics());

      assertThat(metric.getName()).isEqualTo("app.checkout");
      assertThat(metric.getLongSumData().getAggregationTemporality()).isEqualTo(AggregationTemporality.CUMULATIVE);
      assertThat(metric.getInstrumentationScopeInfo().getName()).isEqualTo("custom.scope");
      assertThat(metric.getResource().getAttribute(AttributeKey.stringKey("service.name"))).isEqualTo("catalog");
      assertThat(onlyLongPoint(metric).getValue()).isEqualTo(4);
    }
  }

  private static Map<String, MetricData> byName(Collection<MetricData> metrics) {
    return metrics.stream().collect(Collectors.toMap(MetricData::getName, metric -> metric));
  }

  private static MetricData onlyMetric(Collection<MetricData> metrics) {
    assertThat(metrics).hasSize(1);
    return metrics.iterator().next();
  }

  private static LongPointData onlyLongPoint(MetricData metric) {
    assertThat(metric.getLongSumData().getPoints()).hasSize(1);
    return metric.getLongSumData().getPoints().iterator().next();
  }

  private static LongPointData onlyLongSumPoint(MetricData metric) {
    assertThat(metric.getLongSumData().getPoints()).hasSize(1);
    return metric.getLongSumData().getPoints().iterator().next();
  }

  private static LongPointData onlyLongGaugePoint(MetricData metric) {
    assertThat(metric.getLongGaugeData().getPoints()).hasSize(1);
    return metric.getLongGaugeData().getPoints().iterator().next();
  }

  private static long epochNanos(Instant instant) {
    return instant.getEpochSecond() * 1_000_000_000L + instant.getNano();
  }

  private static final class MutableEpochNanosSource implements LongSupplier {

    private long epochNanos;

    private MutableEpochNanosSource(long epochNanos) {
      this.epochNanos = epochNanos;
    }

    @Override
    public long getAsLong() {
      return epochNanos;
    }

    private void advanceSeconds(long seconds) {
      epochNanos += seconds * 1_000_000_000L;
    }
  }
}
