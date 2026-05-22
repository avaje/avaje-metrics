# avaje-metrics-otel

Convenience module for creating an OTLP-backed `OpenTelemetrySdk` configured with:

- OTLP trace export
- `avaje-metrics-otel-producer` registered as a metric producer
- `avaje-metrics-otel-trace` on the classpath/module path for traced timers
- the same `service.name` resource on both tracer and meter providers
- W3C trace-context propagators

## Maven dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics-otel</artifactId>
  <version>9.9-RC5</version>
</dependency>
```

## Basic usage

```java
OpenTelemetry openTelemetry =
    MetricsOpenTelemetry.builder()
        .endpoint("http://otel-collector:4317")
        .serviceName("my-service")
        .meterInterval(Duration.ofSeconds(60))
        .traceInterval(Duration.ofSeconds(10))
        .buildAndRegisterGlobal();
```

This helper configures:

- a `SdkMeterProvider` with:
  - a `PeriodicMetricReader`
  - an OTLP gRPC metric exporter
  - `OtelMetricProducer`
- a `SdkTracerProvider` with:
  - a batch span processor
  - an OTLP gRPC span exporter
- the traced-timer bridge used by traced timers created via
  `Metrics.timerBuilder(...).buildTraced()`

## Builder options

```java
OpenTelemetrySdk sdk =
    MetricsOpenTelemetry.builder()
        .endpoint("http://otel-collector:4317")   // default: http://localhost:4317
        .serviceName("my-service")                // default: unknown_service:java
        .includeTrace(true)                       // default: true
        .includeMeter(true)                       // default: true
        .timedThresholdMicros(1_000)              // default: 0
        .meterInterval(Duration.ofSeconds(60))    // default: 60s
        .traceInterval(Duration.ofSeconds(10))    // default: 10s
        .registry(Metrics.registry())             // default: Metrics.registry()
        .build();
```

You can also provide custom exporters when you need OTLP-specific configuration such as headers,
compression, or timeouts:

```java
OpenTelemetrySdk sdk =
    MetricsOpenTelemetry.builder()
        .endpoint("http://otel-collector:4317")
        .serviceName("my-service")
        .metricExporter(
            OtlpGrpcMetricExporter.builder()
                .setEndpoint("http://otel-collector:4317")
                .build())
        .spanExporter(
            OtlpGrpcSpanExporter.builder()
                .setEndpoint("http://otel-collector:4317")
                .build())
        .build();
```

## Notes

- Normal OpenTelemetry meters and tracers created from the returned SDK are exported alongside the
  avaje metrics exposed by `OtelMetricProducer`.
- traced timers created via `Metrics.timerBuilder(...).buildTraced()` work by default because this
  module brings in `avaje-metrics-otel-trace`.
- `includeTrace(false)` or `includeMeter(false)` can be used for metrics-only or traces-only setup.
- If you want more control over SDK setup, wire OpenTelemetry manually and use
  `avaje-metrics-otel-producer` directly.
