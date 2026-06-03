# avaje-metrics-otel

Convenience module for creating an OTLP-backed `OpenTelemetrySdk` configured with:

- OTLP trace export, using gRPC by default or HTTP/protobuf when selected
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
  - an OTLP metric exporter
  - `OtelMetricProducer`
- a `SdkTracerProvider` with:
  - a batch span processor
  - an OTLP span exporter
- the traced-timer bridge used by traced timers created via
  `Metrics.timerBuilder(...).buildTraced()` or `buildRootTraced()`

## Builder options

```java
OpenTelemetrySdk sdk =
    MetricsOpenTelemetry.builder()
        .protocol(MetricsOpenTelemetry.Protocol.GRPC) // default: GRPC
        .endpoint("http://otel-collector:4317")   // default: http://localhost:4317
        .serviceName("my-service")                // default: unknown_service:java
        .includeTrace(true)                       // default: true
        .includeMeter(true)                       // default: true
        .timedThresholdMicros(1_000)              // default: 0
        .resourceAttribute("business.domain", "core")
        .resourceAttributes("business.platform=base")
        .deploymentEnvironmentName("production")
        .serviceNamespace("tracking")
        .traceSampleRatio(0.05)                  // parentBased(traceIdRatioBased(0.05))
        .meterInterval(Duration.ofSeconds(60))    // default: 60s
        .traceInterval(Duration.ofSeconds(10))    // default: 10s
        .registry(Metrics.registry())             // default: Metrics.registry()
        .build();
```

By default, the builder uses OTLP gRPC and passes `endpoint(...)` directly to the gRPC
metric and span exporters.

For OTLP HTTP/protobuf, select `HTTP_PROTOBUF` and provide the HTTP base endpoint:

```java
OpenTelemetrySdk sdk =
    MetricsOpenTelemetry.builder()
        .protocol(MetricsOpenTelemetry.Protocol.HTTP_PROTOBUF)
        .endpoint("http://otel-collector:4318")
        .serviceName("my-service")
        .build();
```

In HTTP/protobuf mode, `endpoint(...)` is the base endpoint. The builder appends
`/v1/metrics` for metric export and `/v1/traces` for trace export. If you need
signal-specific endpoints, headers, compression, or timeout configuration, provide
explicit exporters with `metricExporter(...)` and `spanExporter(...)`.

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

This configures the SDK sampler. Use `@Timed(span = Timed.SpanMode.ROOT)` or
`buildRootTraced()` on the top-level handler boundary to start a root span when no
recording span is current. Child traced timers then follow that root sampling decision.
`@Timed(span = Timed.SpanMode.CHILD)` and `buildTraced()` remain no-op when there is no
current recording span.

#### waitIfRunning at end of invocation

AWS Lambda freezes the worker between invocations, which can interrupt an in-flight OTLP
HTTP/gRPC export and cause data loss. Use `enableWaitIfRunning()` to obtain a
`TelemetryWaiter` that, at the end of an invocation:

1. blocks briefly if a scheduled background export is currently in progress, and
2. force-flushes telemetry that has gone stale (no successful background export within
   the configured `flushIfStale` window).

Calling `enableWaitIfRunning()` switches two defaults to Lambda-friendly values, but
only when the caller has not already set them explicitly:

- `meterInterval` → 30 seconds (default is 60 seconds for non-Lambda usage)
- `flushIfStale` → `2 × meterInterval` (60 seconds with the default interval)

In busy environments the periodic reader keeps `lastSuccess` fresh and the stale
forceFlush is a no-op. In low-traffic environments (e.g. a Lambda invoked once a
minute, where the periodic reader is frozen between invocations) the stale forceFlush
ships data on the invocation thread before the runtime is suspended again.

```java
import io.avaje.metrics.otel.MetricsOpenTelemetry;
import io.avaje.metrics.otel.TelemetryWaiter;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

MetricsOpenTelemetry.Result result = MetricsOpenTelemetry.builder()
    .protocol(MetricsOpenTelemetry.Protocol.HTTP_PROTOBUF)
    .endpoint("http://otel-collector:4318")
    .serviceName("orders-lambda")
    .exportTimeout(Duration.ofSeconds(5))
    .connectTimeout(Duration.ofSeconds(2))
    .enableWaitIfRunning()
        .timeout(35, TimeUnit.SECONDS)
        // .flushIfStale(Duration.ofSeconds(60))  // optional, defaults to 2 × meterInterval
        .buildAndRegisterGlobal();

OpenTelemetrySdk sdk = result.sdk();
TelemetryWaiter waiter = result.waiter();

// In the Lambda handler:
public void handle(Event event) {
  try {
    process(event);
  } finally {
    waiter.waitIfRunning();
  }
}
```

Pass `Duration.ZERO` to `flushIfStale(...)` to disable the stale forceFlush and use only
the in-flight wait behaviour.

`connectTimeout` and `exportTimeout` are passed through to the default OTLP HTTP/gRPC
exporters. They have no effect when a custom exporter is supplied via
`metricExporter(...)` or `spanExporter(...)`.

#### Custom exporters

You can also provide custom exporters when you need OTLP-specific configuration such as headers,
signal-specific endpoints, compression, or timeouts:

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
- traced timers created via `Metrics.timerBuilder(...).buildTraced()` or
  `buildRootTraced()` work by default because this module brings in
  `avaje-metrics-otel-trace`.
- `includeTrace(false)` or `includeMeter(false)` can be used for metrics-only or traces-only setup.
- If you want more control over SDK setup, wire OpenTelemetry manually and use
  `avaje-metrics-otel-producer` directly.
