# avaje-metrics-otel

Convenience module for creating an OTLP-backed `OpenTelemetrySdk` configured with:

- OTLP trace export
- `avaje-metrics-otel-producer` registered as a metric producer
- `avaje-metrics-otel-trace` on the classpath/module path for traced timers
- the same `service.name` resource on both tracer and meter providers
- `service.name` from `otel.service.name` / `OTEL_SERVICE_NAME`
- resource attributes from `otel.resource.attributes` / `OTEL_RESOURCE_ATTRIBUTES`
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
        .deploymentEnvironmentName("production")
        .resourceAttributes("business.domain=core,business.platform=base")
        .traceSampleRatio(0.05)
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
        .resourceAttribute("business.domain", "core")
        .resourceAttributes("business.platform=base")
        .deploymentEnvironmentName("production")
        .systemNamespace("tracking")
        .traceSampleRatio(0.05)                  // parentBased(traceIdRatioBased(0.05))
        .meterInterval(Duration.ofSeconds(60))    // default: 60s
        .traceInterval(Duration.ofSeconds(10))    // default: 10s
        .registry(Metrics.registry())             // default: Metrics.registry()
        .build();
```

Resource attributes can also be supplied using the standard OpenTelemetry configuration names:

```bash
java \
  -Dotel.resource.attributes="business.domain=core,business.platform=base,deployment.environment.name=production" \
  -jar app.jar
```

or via an environment variable:

```bash
OTEL_RESOURCE_ATTRIBUTES="business.domain=core,business.platform=base,deployment.environment.name=production"
```

When both are present, the system property `otel.resource.attributes` wins over the environment
variable. The service name can also be supplied using `otel.service.name` or `OTEL_SERVICE_NAME`.
Explicit builder resource attributes override configured resource attributes, `otel.service.name` /
`OTEL_SERVICE_NAME` override `service.name` from resource attributes, and `serviceName(...)`
overrides all configured service names.

## Trace sampling

If no sampler is configured, the OpenTelemetry SDK default applies:
`parentBased(alwaysOn)`. For services with HTTP request spans, this means root request
spans are sampled unless an upstream parent says otherwise.

Set a root trace sample ratio in code:

```java
OpenTelemetrySdk sdk =
    MetricsOpenTelemetry.builder()
        .endpoint("http://otel-collector:4317")
        .serviceName("orders")
        .traceSampleRatio(0.05)
        .buildAndRegisterGlobal();
```

This uses `parentBased(traceIdRatioBased(0.05))`, so incoming sampling decisions are
respected and new root traces are sampled at 5%.

You can also use standard OpenTelemetry configuration:

```bash
OTEL_TRACES_SAMPLER=parentbased_traceidratio
OTEL_TRACES_SAMPLER_ARG=0.05
```

or system properties:

```bash
java \
  -Dotel.traces.sampler=parentbased_traceidratio \
  -Dotel.traces.sampler.arg=0.05 \
  -jar app.jar
```

For full control, provide an SDK sampler directly:

```java
MetricsOpenTelemetry.builder()
  .sampler(Sampler.alwaysOff())
  .build();
```

### Kubernetes with avaje-nima

When using `avaje-nima-opentelemetry`, the Nima filter creates the HTTP SERVER span.
If no trace headers arrive on the request, that SERVER span is a root span and
`traceSampleRatio(...)` controls the sampling rate. Traced timers under sampled
requests are exported as child spans; traced timers under unsampled requests are not.

The Nima filter excludes `/health` by default before tracing.

### AWS Lambda

For Lambda-style applications, configure the same sampler on `MetricsOpenTelemetry`:

```java
MetricsOpenTelemetry.builder()
  .serviceName("orders-lambda")
  .deploymentEnvironmentName("production")
  .traceSampleRatio(0.10)
  .buildAndRegisterGlobal();
```

This configures the SDK sampler. It does not create the Lambda invocation root span by
itself. If Lambda instrumentation or a handler wrapper creates a current invocation
span, avaje traced timers become child spans and follow that sampling decision. If
there is no current recording span, traced timers are no-op.

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
- Resource attributes apply to both metrics and spans.
- traced timers created via `Metrics.timerBuilder(...).buildTraced()` work by default because this
  module brings in `avaje-metrics-otel-trace`.
- `includeTrace(false)` or `includeMeter(false)` can be used for metrics-only or traces-only setup.
- If you want more control over SDK setup, wire OpenTelemetry manually and use
  `avaje-metrics-otel-producer` directly.
