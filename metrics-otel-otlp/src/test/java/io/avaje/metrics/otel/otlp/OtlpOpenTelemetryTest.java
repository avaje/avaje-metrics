package io.avaje.metrics.otel.otlp;

import io.avaje.metrics.Metrics;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpOpenTelemetryTest {

  private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");

  @Test
  void build_collectsProducerAndRegularOtelTelemetry() {
    var metricReader = InMemoryMetricReader.create();
    var spanExporter = InMemorySpanExporter.create();
    var registry = Metrics.createRegistry();

    try (var openTelemetry = OtlpOpenTelemetry.builder()
      .endpoint("http://collector:4317")
      .serviceName("catalog-service")
      .timedThresholdMicros(1_000)
      .registry(registry)
      .metricReader(metricReader)
      .spanExporter(spanExporter)
      .build()) {

      registry.counter("app.login").inc(5);
      registry.timer("app.fast").addEventDuration(true, 500_000L);

      openTelemetry.getMeter("manual").counterBuilder("manual.requests").build().add(2);
      openTelemetry.getTracer("manual").spanBuilder("manual-span").startSpan().end();

      flush(openTelemetry.getSdkTracerProvider().forceFlush());

      Map<String, MetricData> metrics = byName(metricReader.collectAllMetrics());
      assertThat(metrics).containsKeys("app.login", "manual.requests");
      assertThat(metrics).doesNotContainKey("app.fast.count");
      assertThat(metrics.get("app.login").getResource().getAttribute(SERVICE_NAME)).isEqualTo("catalog-service");
      assertThat(metrics.get("manual.requests").getResource().getAttribute(SERVICE_NAME)).isEqualTo("catalog-service");

      var spans = spanExporter.getFinishedSpanItems();
      assertThat(spans).hasSize(1);
      assertThat(spans.get(0).getName()).isEqualTo("manual-span");
      assertThat(spans.get(0).getResource().getAttribute(SERVICE_NAME)).isEqualTo("catalog-service");
    }
  }

  @Test
  void defaults_applyToResourceAndProducer() {
    var metricReader = InMemoryMetricReader.create();
    var spanExporter = InMemorySpanExporter.create();
    var registry = Metrics.createRegistry();

    try (var openTelemetry = OtlpOpenTelemetry.builder()
      .registry(registry)
      .metricReader(metricReader)
      .spanExporter(spanExporter)
      .build()) {

      registry.counter("app.requests").inc(3);
      openTelemetry.getTracer("manual").spanBuilder("default-span").startSpan().end();

      flush(openTelemetry.getSdkTracerProvider().forceFlush());

      MetricData metric = onlyMetric(metricReader.collectAllMetrics());
      assertThat(metric.getName()).isEqualTo("app.requests");
      assertThat(metric.getResource().getAttribute(SERVICE_NAME)).isEqualTo("unknown_service:java");
      assertThat(spanExporter.getFinishedSpanItems()).hasSize(1);
      assertThat(spanExporter.getFinishedSpanItems().get(0).getResource().getAttribute(SERVICE_NAME))
        .isEqualTo("unknown_service:java");
    }
  }

  @Test
  void includeTraceFalse_skipsSpanExport() {
    var metricReader = InMemoryMetricReader.create();
    var spanExporter = InMemorySpanExporter.create();
    var registry = Metrics.createRegistry();

    try (var openTelemetry = OtlpOpenTelemetry.builder()
      .serviceName("catalog-service")
      .registry(registry)
      .metricReader(metricReader)
      .spanExporter(spanExporter)
      .includeTrace(false)
      .build()) {

      registry.counter("app.requests").inc(2);
      openTelemetry.getTracer("manual").spanBuilder("manual-span").startSpan().end();

      assertThat(byName(metricReader.collectAllMetrics())).containsKey("app.requests");
      assertThat(spanExporter.getFinishedSpanItems()).isEmpty();
    }
  }

  @Test
  void includeMeterFalse_skipsMetricExport() {
    var metricReader = InMemoryMetricReader.create();
    var spanExporter = InMemorySpanExporter.create();
    var registry = Metrics.createRegistry();

    try (var openTelemetry = OtlpOpenTelemetry.builder()
      .serviceName("catalog-service")
      .registry(registry)
      .metricReader(metricReader)
      .spanExporter(spanExporter)
      .includeMeter(false)
      .build()) {

      registry.counter("app.requests").inc(2);
      openTelemetry.getMeter("manual").counterBuilder("manual.requests").build().add(1);
      openTelemetry.getTracer("manual").spanBuilder("manual-span").startSpan().end();

      flush(openTelemetry.getSdkTracerProvider().forceFlush());

      assertThat(metricReader.collectAllMetrics()).isEmpty();
      assertThat(spanExporter.getFinishedSpanItems()).hasSize(1);
    }
  }

  @Test
  void metricExporterOverride_isUsed() {
    var metricExporter = new CapturingMetricExporter();
    var spanExporter = InMemorySpanExporter.create();
    var registry = Metrics.createRegistry();

    try (var openTelemetry = OtlpOpenTelemetry.builder()
      .serviceName("catalog-service")
      .registry(registry)
      .metricExporter(metricExporter)
      .spanExporter(spanExporter)
      .build()) {

      registry.counter("app.requests").inc(2);

      flush(openTelemetry.getSdkMeterProvider().forceFlush());

      assertThat(byName(metricExporter.exportedMetrics())).containsKey("app.requests");
    }
  }

  private static void flush(CompletableResultCode resultCode) {
    resultCode.join(5, TimeUnit.SECONDS);
    assertThat(resultCode.isSuccess()).isTrue();
  }

  private static Map<String, MetricData> byName(Collection<MetricData> metrics) {
    return metrics.stream().collect(Collectors.toMap(MetricData::getName, metric -> metric));
  }

  private static MetricData onlyMetric(Collection<MetricData> metrics) {
    assertThat(metrics).hasSize(1);
    return metrics.iterator().next();
  }

  private static final class CapturingMetricExporter implements MetricExporter {

    private Collection<MetricData> exportedMetrics = List.of();

    @Override
    public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
      return AggregationTemporality.DELTA;
    }

    @Override
    public CompletableResultCode export(Collection<MetricData> metrics) {
      exportedMetrics = List.copyOf(metrics);
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
      return CompletableResultCode.ofSuccess();
    }

    Collection<MetricData> exportedMetrics() {
      return exportedMetrics;
    }
  }
}
