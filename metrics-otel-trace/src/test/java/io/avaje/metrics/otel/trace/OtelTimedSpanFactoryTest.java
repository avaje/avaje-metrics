package io.avaje.metrics.otel.trace;

import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.Metrics;
import io.avaje.metrics.Tags;
import io.avaje.metrics.Timer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.context.Scope;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OtelTimedSpanFactoryTest {

  InMemorySpanExporter exporter;
  SdkTracerProvider tracerProvider;
  OpenTelemetrySdk openTelemetry;

  @BeforeEach
  void setup() {
    GlobalOpenTelemetry.resetForTest();
    exporter = InMemorySpanExporter.create();
    tracerProvider = SdkTracerProvider.builder()
      .addSpanProcessor(SimpleSpanProcessor.create(exporter))
      .build();
    openTelemetry = OpenTelemetrySdk.builder()
      .setTracerProvider(tracerProvider)
      .build();
    GlobalOpenTelemetry.set(openTelemetry);
  }

  @AfterEach
  void teardown() {
    GlobalOpenTelemetry.resetForTest();
    tracerProvider.close();
  }

  @Test
  void tracedTimer_successSpan() {
    MetricRegistry registry = Metrics.createRegistry();
    Timer timer = registry.timerBuilder("app.service.method")
      .tags(Tags.of("env:prod", "region:ap-south"))
      .buildTraced();

    var parent = openTelemetry.getTracer("test").spanBuilder("parent").startSpan();
    try (Scope ignored = parent.makeCurrent()) {
      timer.time(() -> "ok");
    } finally {
      parent.end();
    }

    List<SpanData> spans = exporter.getFinishedSpanItems();
    assertThat(spans).hasSize(2);
    SpanData span = spans.stream()
      .filter(it -> it.getName().equals("app.service.method"))
      .findFirst()
      .orElseThrow();
    assertThat(span.getName()).isEqualTo("app.service.method");
    assertThat(span.getStatus().getStatusCode()).isNotEqualTo(StatusCode.ERROR);
    assertThat(span.getAttributes().get(AttributeKey.stringKey("avaje.metrics.name"))).isEqualTo("app.service.method");
    assertThat(span.getAttributes().get(AttributeKey.stringKey("env"))).isEqualTo("prod");
    assertThat(span.getAttributes().get(AttributeKey.stringKey("region"))).isEqualTo("ap-south");
  }

  @Test
  void tracedTimer_errorSpan() {
    MetricRegistry registry = Metrics.createRegistry();
    Timer timer = registry.tracedTimer("app.service.method");

    var parent = openTelemetry.getTracer("test").spanBuilder("parent").startSpan();
    try (Scope ignored = parent.makeCurrent()) {
      Timer.Event event = timer.startEvent();
      event.endWithError();
    } finally {
      parent.end();
    }

    List<SpanData> spans = exporter.getFinishedSpanItems();
    assertThat(spans).hasSize(2);
    SpanData span = spans.stream()
      .filter(it -> it.getName().equals("app.service.method"))
      .findFirst()
      .orElseThrow();
    assertThat(span.getStatus().getStatusCode()).isEqualTo(StatusCode.ERROR);
  }

  @Test
  void tracedTimer_errorSpanRecordsThrowable() {
    MetricRegistry registry = Metrics.createRegistry();
    Timer timer = registry.tracedTimer("app.service.method");
    var error = new IllegalStateException("boom");

    var parent = openTelemetry.getTracer("test").spanBuilder("parent").startSpan();
    try (Scope ignored = parent.makeCurrent()) {
      Timer.Event event = timer.startEvent();
      event.endWithError(error);
    } finally {
      parent.end();
    }

    List<SpanData> spans = exporter.getFinishedSpanItems();
    assertThat(spans).hasSize(2);
    SpanData span = spans.stream()
      .filter(it -> it.getName().equals("app.service.method"))
      .findFirst()
      .orElseThrow();
    assertThat(span.getStatus().getStatusCode()).isEqualTo(StatusCode.ERROR);
    assertThat(span.getEvents()).singleElement().satisfies(event -> {
      assertThat(event.getName()).isEqualTo("exception");
      assertThat(event.getAttributes().get(AttributeKey.stringKey("exception.type"))).isEqualTo(IllegalStateException.class.getName());
      assertThat(event.getAttributes().get(AttributeKey.stringKey("exception.message"))).isEqualTo("boom");
    });
  }

  @Test
  void tracedTimer_labelTagUsesMethodLabelForSpanName() {
    MetricRegistry registry = Metrics.createRegistry();
    Timer timer = registry.timerBuilder("web.api")
      .tags(Tags.of("label:CustomerResource.staticGeneral", "env:prod"))
      .buildTraced();

    var parent = openTelemetry.getTracer("test").spanBuilder("parent").startSpan();
    try (Scope ignored = parent.makeCurrent()) {
      timer.time(() -> "ok");
    } finally {
      parent.end();
    }

    List<SpanData> spans = exporter.getFinishedSpanItems();
    assertThat(spans).hasSize(2);
    SpanData span = spans.stream()
      .filter(it -> it.getName().equals("CustomerResource.staticGeneral"))
      .findFirst()
      .orElseThrow();
    assertThat(span.getAttributes().get(AttributeKey.stringKey("avaje.metrics.name"))).isEqualTo("web.api");
    assertThat(span.getAttributes().get(AttributeKey.stringKey("label"))).isEqualTo("CustomerResource.staticGeneral");
    assertThat(span.getAttributes().get(AttributeKey.stringKey("env"))).isEqualTo("prod");
  }

  @Test
  void tracedTimer_withoutGlobalTelemetry_isNoop() {
    MetricRegistry registry = Metrics.createRegistry();
    GlobalOpenTelemetry.resetForTest();

    registry.tracedTimer("app.service.method").time(() -> "ok");

    assertThat(exporter.getFinishedSpanItems()).isEmpty();
  }

  @Test
  void tracedTimer_withoutRecordingParent_isNoop() {
    MetricRegistry registry = Metrics.createRegistry();

    registry.tracedTimer("app.service.method").time(() -> "ok");

    assertThat(exporter.getFinishedSpanItems()).isEmpty();
  }

  @Test
  void preparedSpan_usesGlobalTelemetryAtStartTime() {
    var factory = new OtelTimedSpanFactory();
    GlobalOpenTelemetry.resetForTest();
    var prepared = factory.prepare(Metric.ID.of("app.service.method"), null);

    GlobalOpenTelemetry.set(openTelemetry);
    var parent = openTelemetry.getTracer("test").spanBuilder("parent").startSpan();
    try (Scope ignored = parent.makeCurrent()) {
      prepared.start().end();
    } finally {
      parent.end();
    }

    List<SpanData> spans = exporter.getFinishedSpanItems();
    assertThat(spans).hasSize(2);
    assertThat(spans.stream().anyMatch(it -> it.getName().equals("app.service.method"))).isTrue();
  }
}
