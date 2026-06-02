package io.avaje.metrics.otel;

import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.Metrics;
import io.avaje.metrics.Timer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.opentelemetry.context.Scope;

import static io.avaje.metrics.otel.MetricsOpenTelemetry.Protocol.HTTP_PROTOBUF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetricsOpenTelemetryTest {

  private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");
  private static final AttributeKey<String> BUSINESS_DOMAIN = AttributeKey.stringKey("business.domain");
  private static final AttributeKey<String> BUSINESS_PLATFORM = AttributeKey.stringKey("business.platform");
  private static final AttributeKey<String> BUSINESS_SYSTEM = AttributeKey.stringKey("business.system");
  private static final AttributeKey<String> DEPLOYMENT_ENVIRONMENT_NAME =
    AttributeKey.stringKey("deployment.environment.name");
  private static final AttributeKey<String> SYSTEM_NAMESPACE = AttributeKey.stringKey("system.namespace");

  @AfterEach
  void tearDown() {
    System.clearProperty(ResourceAttributes.RESOURCE_ATTRIBUTES_PROPERTY);
    System.clearProperty(ResourceAttributes.SERVICE_NAME_PROPERTY);
    System.clearProperty(TraceSampling.TRACES_SAMPLER_PROPERTY);
    System.clearProperty(TraceSampling.TRACES_SAMPLER_ARG_PROPERTY);
  }

  @Test
  void build_collectsProducerAndRegularOtelTelemetry() {
    var metricReader = InMemoryMetricReader.create();
    var spanExporter = InMemorySpanExporter.create();
    var registry = Metrics.createRegistry();

    try (var openTelemetry = MetricsOpenTelemetry.builder()
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

    try (var openTelemetry = MetricsOpenTelemetry.builder()
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
  void systemPropertyResourceAttributes_applyToMetricAndSpanResources() {
    System.setProperty(
      ResourceAttributes.RESOURCE_ATTRIBUTES_PROPERTY,
      "service.name=property-service,business.domain=fleet,deployment.environment.name=production");
    var metricReader = InMemoryMetricReader.create();
    var spanExporter = InMemorySpanExporter.create();
    var registry = Metrics.createRegistry();

    try (var openTelemetry = MetricsOpenTelemetry.builder()
      .registry(registry)
      .metricReader(metricReader)
      .spanExporter(spanExporter)
      .build()) {

      registry.counter("app.requests").inc(3);
      openTelemetry.getTracer("manual").spanBuilder("resource-span").startSpan().end();

      flush(openTelemetry.getSdkTracerProvider().forceFlush());

      var metricResource = onlyMetric(metricReader.collectAllMetrics()).getResource();
      assertThat(metricResource.getAttribute(SERVICE_NAME)).isEqualTo("property-service");
      assertThat(metricResource.getAttribute(BUSINESS_DOMAIN)).isEqualTo("fleet");
      assertThat(metricResource.getAttribute(DEPLOYMENT_ENVIRONMENT_NAME)).isEqualTo("production");

      var spanResource = spanExporter.getFinishedSpanItems().get(0).getResource();
      assertThat(spanResource.getAttribute(SERVICE_NAME)).isEqualTo("property-service");
      assertThat(spanResource.getAttribute(BUSINESS_DOMAIN)).isEqualTo("fleet");
      assertThat(spanResource.getAttribute(DEPLOYMENT_ENVIRONMENT_NAME)).isEqualTo("production");
    }
  }

  @Test
  void builderResourceAttributes_applyToMetricAndSpanResources() {
    var metricReader = InMemoryMetricReader.create();
    var spanExporter = InMemorySpanExporter.create();
    var registry = Metrics.createRegistry();

    try (var openTelemetry = MetricsOpenTelemetry.builder()
      .serviceName("catalog-service")
      .resourceAttributes(Map.of("business.domain", "fleet"))
      .resourceAttributes("business.platform=ship")
      .resourceAttribute("business.system", "vehicle-tracking")
      .deploymentEnvironmentName("production")
      .systemNamespace("tracking")
      .registry(registry)
      .metricReader(metricReader)
      .spanExporter(spanExporter)
      .build()) {

      registry.counter("app.requests").inc(3);
      openTelemetry.getTracer("manual").spanBuilder("resource-span").startSpan().end();

      flush(openTelemetry.getSdkTracerProvider().forceFlush());

      var metricResource = onlyMetric(metricReader.collectAllMetrics()).getResource();
      assertThat(metricResource.getAttribute(SERVICE_NAME)).isEqualTo("catalog-service");
      assertThat(metricResource.getAttribute(BUSINESS_DOMAIN)).isEqualTo("fleet");
      assertThat(metricResource.getAttribute(BUSINESS_PLATFORM)).isEqualTo("ship");
      assertThat(metricResource.getAttribute(BUSINESS_SYSTEM)).isEqualTo("vehicle-tracking");
      assertThat(metricResource.getAttribute(DEPLOYMENT_ENVIRONMENT_NAME)).isEqualTo("production");
      assertThat(metricResource.getAttribute(SYSTEM_NAMESPACE)).isEqualTo("tracking");

      var spanResource = spanExporter.getFinishedSpanItems().get(0).getResource();
      assertThat(spanResource.getAttribute(SERVICE_NAME)).isEqualTo("catalog-service");
      assertThat(spanResource.getAttribute(BUSINESS_DOMAIN)).isEqualTo("fleet");
      assertThat(spanResource.getAttribute(BUSINESS_PLATFORM)).isEqualTo("ship");
      assertThat(spanResource.getAttribute(BUSINESS_SYSTEM)).isEqualTo("vehicle-tracking");
      assertThat(spanResource.getAttribute(DEPLOYMENT_ENVIRONMENT_NAME)).isEqualTo("production");
      assertThat(spanResource.getAttribute(SYSTEM_NAMESPACE)).isEqualTo("tracking");
    }
  }

  @Test
  void serviceName_overridesResourceAttributeServiceName() {
    var metricReader = InMemoryMetricReader.create();
    var registry = Metrics.createRegistry();

    try (var openTelemetry = MetricsOpenTelemetry.builder()
      .resourceAttribute("service.name", "resource-service")
      .serviceName("builder-service")
      .registry(registry)
      .metricReader(metricReader)
      .includeTrace(false)
      .build()) {

      registry.counter("app.requests").inc(3);

      var metricResource = onlyMetric(metricReader.collectAllMetrics()).getResource();
      assertThat(metricResource.getAttribute(SERVICE_NAME)).isEqualTo("builder-service");
    }
  }

  @Test
  void configuredServiceName_overridesResourceAttributeServiceName() {
    System.setProperty(ResourceAttributes.SERVICE_NAME_PROPERTY, "configured-service");
    var metricReader = InMemoryMetricReader.create();
    var registry = Metrics.createRegistry();

    try (var openTelemetry = MetricsOpenTelemetry.builder()
      .resourceAttribute("service.name", "resource-service")
      .registry(registry)
      .metricReader(metricReader)
      .includeTrace(false)
      .build()) {

      registry.counter("app.requests").inc(3);

      var metricResource = onlyMetric(metricReader.collectAllMetrics()).getResource();
      assertThat(metricResource.getAttribute(SERVICE_NAME)).isEqualTo("configured-service");
    }
  }

  @Test
  void serviceName_overridesConfiguredServiceName() {
    System.setProperty(ResourceAttributes.SERVICE_NAME_PROPERTY, "configured-service");
    var metricReader = InMemoryMetricReader.create();
    var registry = Metrics.createRegistry();

    try (var openTelemetry = MetricsOpenTelemetry.builder()
      .serviceName("builder-service")
      .registry(registry)
      .metricReader(metricReader)
      .includeTrace(false)
      .build()) {

      registry.counter("app.requests").inc(3);

      var metricResource = onlyMetric(metricReader.collectAllMetrics()).getResource();
      assertThat(metricResource.getAttribute(SERVICE_NAME)).isEqualTo("builder-service");
    }
  }

  @Test
  void convenienceResourceAttributes_followNormalCallOrder() {
    var metricReader = InMemoryMetricReader.create();
    var registry = Metrics.createRegistry();

    try (var openTelemetry = MetricsOpenTelemetry.builder()
      .deploymentEnvironmentName("production")
      .resourceAttribute("deployment.environment.name", "staging")
      .systemNamespace("tracking")
      .resourceAttribute("system.namespace", "fleet")
      .registry(registry)
      .metricReader(metricReader)
      .includeTrace(false)
      .build()) {

      registry.counter("app.requests").inc(3);

      var metricResource = onlyMetric(metricReader.collectAllMetrics()).getResource();
      assertThat(metricResource.getAttribute(DEPLOYMENT_ENVIRONMENT_NAME)).isEqualTo("staging");
      assertThat(metricResource.getAttribute(SYSTEM_NAMESPACE)).isEqualTo("fleet");
    }
  }

  @Test
  void resourceAttributes_parseAndDecodeValues() {
    var attributes = ResourceAttributes.parse(
      "business.domain=fleet%20ops,business.platform=ship+track,broken=keep%2Gvalue,,");

    assertThat(attributes)
      .containsEntry("business.domain", "fleet ops")
      .containsEntry("business.platform", "ship+track")
      .containsEntry("broken", "keep%2Gvalue");
  }

  @Test
  void resourceAttributes_malformedEntry_failsClearly() {
    assertThatThrownBy(() -> MetricsOpenTelemetry.builder().resourceAttributes("business.domain"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("expected key=value");
  }

  @Test
  void traceSampleRatio_zero_suppressesRootSpans() {
    var spanExporter = InMemorySpanExporter.create();

    try (var openTelemetry = MetricsOpenTelemetry.builder()
      .includeMeter(false)
      .spanExporter(spanExporter)
      .traceSampleRatio(0)
      .build()) {

      openTelemetry.getTracer("manual").spanBuilder("root").startSpan().end();
      flush(openTelemetry.getSdkTracerProvider().forceFlush());

      assertThat(spanExporter.getFinishedSpanItems()).isEmpty();
    }
  }

  @Test
  void traceSampleRatio_one_exportsRootSpans() {
    var spanExporter = InMemorySpanExporter.create();

    try (var openTelemetry = MetricsOpenTelemetry.builder()
      .includeMeter(false)
      .spanExporter(spanExporter)
      .traceSampleRatio(1)
      .build()) {

      openTelemetry.getTracer("manual").spanBuilder("root").startSpan().end();
      flush(openTelemetry.getSdkTracerProvider().forceFlush());

      assertThat(spanExporter.getFinishedSpanItems()).singleElement()
        .satisfies(span -> assertThat(span.getName()).isEqualTo("root"));
    }
  }

  @Test
  void traceSampleRatio_respectsSampledParent() {
    var spanExporter = InMemorySpanExporter.create();

    try (var openTelemetry = MetricsOpenTelemetry.builder()
      .includeMeter(false)
      .spanExporter(spanExporter)
      .traceSampleRatio(0)
      .build()) {

      var parent = Span.wrap(SpanContext.createFromRemoteParent(
        "00000000000000000000000000000001",
        "0000000000000001",
        TraceFlags.getSampled(),
        TraceState.getDefault()));
      try (Scope ignored = parent.makeCurrent()) {
        openTelemetry.getTracer("manual").spanBuilder("child").startSpan().end();
      }
      flush(openTelemetry.getSdkTracerProvider().forceFlush());

      assertThat(spanExporter.getFinishedSpanItems()).singleElement()
        .satisfies(span -> assertThat(span.getName()).isEqualTo("child"));
    }
  }

  @Test
  void samplerOverride_isUsed() {
    var spanExporter = InMemorySpanExporter.create();

    try (var openTelemetry = MetricsOpenTelemetry.builder()
      .includeMeter(false)
      .spanExporter(spanExporter)
      .traceSampleRatio(1)
      .sampler(Sampler.alwaysOff())
      .build()) {

      openTelemetry.getTracer("manual").spanBuilder("root").startSpan().end();
      flush(openTelemetry.getSdkTracerProvider().forceFlush());

      assertThat(spanExporter.getFinishedSpanItems()).isEmpty();
    }
  }

  @Test
  void configuredSamplerFromSystemProperties_isUsed() {
    System.setProperty(TraceSampling.TRACES_SAMPLER_PROPERTY, "parentbased_traceidratio");
    System.setProperty(TraceSampling.TRACES_SAMPLER_ARG_PROPERTY, "0");
    var spanExporter = InMemorySpanExporter.create();

    try (var openTelemetry = MetricsOpenTelemetry.builder()
      .includeMeter(false)
      .spanExporter(spanExporter)
      .build()) {

      openTelemetry.getTracer("manual").spanBuilder("root").startSpan().end();
      flush(openTelemetry.getSdkTracerProvider().forceFlush());

      assertThat(spanExporter.getFinishedSpanItems()).isEmpty();
    }
  }

  @Test
  void traceSampleRatio_overridesConfiguredSampler() {
    System.setProperty(TraceSampling.TRACES_SAMPLER_PROPERTY, "always_off");
    var spanExporter = InMemorySpanExporter.create();

    try (var openTelemetry = MetricsOpenTelemetry.builder()
      .includeMeter(false)
      .spanExporter(spanExporter)
      .traceSampleRatio(1)
      .build()) {

      openTelemetry.getTracer("manual").spanBuilder("root").startSpan().end();
      flush(openTelemetry.getSdkTracerProvider().forceFlush());

      assertThat(spanExporter.getFinishedSpanItems()).hasSize(1);
    }
  }

  @Test
  void traceSampleRatio_invalid_failsClearly() {
    assertThatThrownBy(() -> MetricsOpenTelemetry.builder().traceSampleRatio(1.1))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("between 0.0 and 1.0");
  }

  @Test
  void configuredSampler_unsupportedName_failsClearly() {
    System.setProperty(TraceSampling.TRACES_SAMPLER_PROPERTY, "unsupported");

    assertThatThrownBy(() -> MetricsOpenTelemetry.builder()
      .includeMeter(false)
      .spanExporter(InMemorySpanExporter.create())
      .build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Unsupported trace sampler");
  }

  @Test
  void configuredSampler_invalidRatio_failsClearly() {
    System.setProperty(TraceSampling.TRACES_SAMPLER_PROPERTY, "parentbased_traceidratio");
    System.setProperty(TraceSampling.TRACES_SAMPLER_ARG_PROPERTY, "bad");

    assertThatThrownBy(() -> MetricsOpenTelemetry.builder()
      .includeMeter(false)
      .spanExporter(InMemorySpanExporter.create())
      .build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Invalid trace sample ratio");
  }

  @Test
  void includeTraceFalse_skipsSpanExport() {
    var metricReader = InMemoryMetricReader.create();
    var spanExporter = InMemorySpanExporter.create();
    var registry = Metrics.createRegistry();

    try (var openTelemetry = MetricsOpenTelemetry.builder()
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

    try (var openTelemetry = MetricsOpenTelemetry.builder()
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
  void customExporters_bypassProtocolEndpointConstruction() {
    var metricExporter = new CapturingMetricExporter();
    var spanExporter = InMemorySpanExporter.create();
    var registry = Metrics.createRegistry();

    try (var openTelemetry = MetricsOpenTelemetry.builder()
      .serviceName("catalog-service")
      .protocol(HTTP_PROTOBUF)
      .endpoint("not a uri")
      .registry(registry)
      .metricExporter(metricExporter)
      .spanExporter(spanExporter)
      .build()) {

      registry.counter("app.requests").inc(2);

      flush(openTelemetry.getSdkMeterProvider().forceFlush());

      assertThat(byName(metricExporter.exportedMetrics())).containsKey("app.requests");
    }
  }

  @Test
  void defaultProtocol_usesGrpcEndpoint() {
    var builder = MetricsOpenTelemetry.builder();

    assertThat(builder.metricExporterEndpoint()).isEqualTo("http://localhost:4317");
    assertThat(builder.spanExporterEndpoint()).isEqualTo("http://localhost:4317");
  }

  @Test
  void httpProtocol_usesSignalEndpoints() {
    var builder = MetricsOpenTelemetry.builder()
      .protocol(HTTP_PROTOBUF)
      .endpoint("http://otel-collector:4318");

    assertThat(builder.metricExporterEndpoint()).isEqualTo("http://otel-collector:4318/v1/metrics");
    assertThat(builder.spanExporterEndpoint()).isEqualTo("http://otel-collector:4318/v1/traces");
  }

  @Test
  void httpProtocol_usesHttpDefaultEndpoint() {
    var builder = MetricsOpenTelemetry.builder()
      .protocol(HTTP_PROTOBUF);

    assertThat(builder.metricExporterEndpoint()).isEqualTo("http://localhost:4318/v1/metrics");
    assertThat(builder.spanExporterEndpoint()).isEqualTo("http://localhost:4318/v1/traces");
  }

  @Test
  void httpProtocol_allowsTrailingSlashEndpoint() {
    var builder = MetricsOpenTelemetry.builder()
      .protocol(HTTP_PROTOBUF)
      .endpoint("http://otel-collector:4318/");

    assertThat(builder.metricExporterEndpoint()).isEqualTo("http://otel-collector:4318/v1/metrics");
    assertThat(builder.spanExporterEndpoint()).isEqualTo("http://otel-collector:4318/v1/traces");
  }

  @Test
  void buildAndRegisterGlobal_enablesTracedTimersByDefault() {
    GlobalOpenTelemetry.resetForTest();
    var metricReader = InMemoryMetricReader.create();
    var spanExporter = InMemorySpanExporter.create();
    var registry = Metrics.createRegistry();

    try (var openTelemetry = MetricsOpenTelemetry.builder()
      .serviceName("catalog-service")
      .registry(registry)
      .metricReader(metricReader)
      .spanExporter(spanExporter)
      .buildAndRegisterGlobal()) {

      MetricRegistry tracedRegistry = Metrics.createRegistry();
      Timer timer = tracedRegistry.timerBuilder("app.service.method").buildTraced();

      var parent = openTelemetry.getTracer("manual").spanBuilder("parent").startSpan();
      try (Scope ignored = parent.makeCurrent()) {
        timer.time(() -> "ok");
      } finally {
        parent.end();
      }

      flush(openTelemetry.getSdkTracerProvider().forceFlush());

      assertThat(spanExporter.getFinishedSpanItems())
        .extracting(it -> it.getName())
        .contains("parent", "app.service.method");
    } finally {
      GlobalOpenTelemetry.resetForTest();
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
