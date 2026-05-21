package io.avaje.metrics.otel.reporter;

import io.avaje.metrics.*;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class OtelReporterTest {

  InMemoryMetricReader reader;
  SdkMeterProvider sdkMeterProvider;
  OpenTelemetrySdk openTelemetry;
  MetricRegistry registry;
  OtelReporter reporter;

  @BeforeEach
  void setup() {
    reader = InMemoryMetricReader.create();
    sdkMeterProvider = SdkMeterProvider.builder()
        .registerMetricReader(reader)
        .build();
    openTelemetry = OpenTelemetrySdk.builder()
        .setMeterProvider(sdkMeterProvider)
        .build();
    registry = Metrics.createRegistry();
    reporter = OtelReporter.builder()
        .openTelemetry(openTelemetry)
        .registry(registry)
        .build();
  }

  @AfterEach
  void teardown() {
    reporter.close();
    sdkMeterProvider.close();
  }

  @Test
  void counter() {
    Counter counter = registry.counter("app.login.count");
    counter.inc(5);
    counter.inc(3);

    reporter.report();

    Map<String, MetricData> metrics = collectByName();
    assertThat(metrics).containsKey("app.login.count");
    MetricData data = metrics.get("app.login.count");
    assertThat(data.getLongSumData().getPoints()).hasSize(1);
    LongPointData point = data.getLongSumData().getPoints().iterator().next();
    assertThat(point.getValue()).isEqualTo(8);
  }

  @Test
  void counter_withTags() {
    Counter counter = registry.counter("app.login.count", Tags.of("env:prod", "region:us-east"));
    counter.inc(10);

    reporter.report();

    Map<String, MetricData> metrics = collectByName();
    assertThat(metrics).containsKey("app.login.count");
    MetricData data = metrics.get("app.login.count");
    LongPointData point = data.getLongSumData().getPoints().iterator().next();
    assertThat(point.getAttributes().get(io.opentelemetry.api.common.AttributeKey.stringKey("env"))).isEqualTo("prod");
    assertThat(point.getAttributes().get(io.opentelemetry.api.common.AttributeKey.stringKey("region"))).isEqualTo("us-east");
  }

  @Test
  void counter_withNullTagEntry_skipped() {
    Counter counter = registry.counter("app.login.count", Tags.of(null, "env:prod"));
    counter.inc(10);

    reporter.report();

    Map<String, MetricData> metrics = collectByName();
    assertThat(metrics).containsKey("app.login.count");
    MetricData data = metrics.get("app.login.count");
    LongPointData point = data.getLongSumData().getPoints().iterator().next();
    assertThat(point.getAttributes().get(io.opentelemetry.api.common.AttributeKey.stringKey("env"))).isEqualTo("prod");
    assertThat(point.getAttributes().size()).isEqualTo(1);
  }

  @Test
  void counter_zeroNotReported() {
    registry.counter("app.noop.count");
    // no increments

    reporter.report();

    Map<String, MetricData> metrics = collectByName();
    assertThat(metrics).doesNotContainKey("app.noop.count");
  }

  @Test
  void timer() {
    Timer timer = registry.timer("app.service.method");
    timer.addEventDuration(true, 5_000_000L);   // 5ms = 5000 micros
    timer.addEventDuration(true, 10_000_000L);  // 10ms = 10000 micros

    reporter.report();

    Map<String, MetricData> metrics = collectByName();
    assertThat(metrics).containsKey("app.service.method.count");
    assertThat(metrics).containsKey("app.service.method.total");
    assertThat(metrics).containsKey("app.service.method.max");

    LongPointData count = metrics.get("app.service.method.count").getLongSumData().getPoints().iterator().next();
    assertThat(count.getValue()).isEqualTo(2);

    LongPointData total = metrics.get("app.service.method.total").getLongSumData().getPoints().iterator().next();
    assertThat(total.getValue()).isEqualTo(15_000L); // 5000 + 10000 micros
  }

  @Test
  void timer_errorSeparateInstruments() {
    Timer timer = registry.timer("app.service.method");
    timer.addEventDuration(true, 5_000_000L);
    timer.addEventDuration(false, 2_000_000L);  // error

    reporter.report();

    Map<String, MetricData> metrics = collectByName();
    // success instruments
    assertThat(metrics).containsKey("app.service.method.count");
    // error instruments (avaje appends .error to the name)
    assertThat(metrics).containsKey("app.service.method.error.count");
  }

  @Test
  void timer_threshold_skipped() {
    reporter.close();
    reporter = OtelReporter.builder()
        .openTelemetry(openTelemetry)
        .registry(registry)
        .timedThresholdMicros(10_000)  // 10ms threshold
        .build();

    Timer timer = registry.timer("app.fast.method");
    timer.addEventDuration(true, 1_000_000L);  // 1ms — below threshold

    reporter.report();

    Map<String, MetricData> metrics = collectByName();
    assertThat(metrics).doesNotContainKey("app.fast.method.count");
  }

  @Test
  void meter() {
    Meter meter = registry.meter("app.bytes.sent");
    meter.addEvent(1024);
    meter.addEvent(2048);

    reporter.report();

    Map<String, MetricData> metrics = collectByName();
    assertThat(metrics).containsKey("app.bytes.sent.count");
    assertThat(metrics).containsKey("app.bytes.sent.total");
    assertThat(metrics).containsKey("app.bytes.sent.max");

    LongPointData count = metrics.get("app.bytes.sent.count").getLongSumData().getPoints().iterator().next();
    assertThat(count.getValue()).isEqualTo(2);

    LongPointData total = metrics.get("app.bytes.sent.total").getLongSumData().getPoints().iterator().next();
    assertThat(total.getValue()).isEqualTo(3072L);
  }

  @Test
  void gaugeLong() {
    registry.gauge("jvm.threads.active", () -> 42L);

    reporter.report();

    Map<String, MetricData> metrics = collectByName();
    assertThat(metrics).containsKey("jvm.threads.active");
    long value = metrics.get("jvm.threads.active").getLongGaugeData().getPoints().iterator().next().getValue();
    assertThat(value).isEqualTo(42L);
  }

  @Test
  void gaugeDouble() {
    registry.gauge("jvm.memory.pct", () -> 0.75);

    reporter.report();

    Map<String, MetricData> metrics = collectByName();
    assertThat(metrics).containsKey("jvm.memory.pct");
    double value = metrics.get("jvm.memory.pct").getDoubleGaugeData().getPoints().iterator().next().getValue();
    assertThat(value).isEqualTo(0.75);
  }

  @Test
  void multipleReportCycles_counterAccumulates() {
    Counter counter = registry.counter("app.requests");

    counter.inc(5);
    reporter.report();
    counter.inc(3);
    reporter.report();

    Map<String, MetricData> metrics = collectByName();
    LongPointData point = metrics.get("app.requests").getLongSumData().getPoints().iterator().next();
    // OTEL SDK accumulates deltas: 5 + 3 = 8 total
    assertThat(point.getValue()).isEqualTo(8);
  }

  private Map<String, MetricData> collectByName() {
    Collection<MetricData> all = reader.collectAllMetrics();
    return all.stream().collect(Collectors.toMap(MetricData::getName, m -> m));
  }
}
