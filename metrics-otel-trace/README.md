# avaje-metrics-otel-trace

Provides the optional OpenTelemetry span bridge used by traced timers.

When this module is on the classpath or module path, `Metrics.tracedTimer(...)` and enhancement
with `@Timed(span = Timed.SpanMode.ON)` create OpenTelemetry spans via `GlobalOpenTelemetry`.

This module does **not** export avaje metrics to OpenTelemetry metrics backends. For that use:

- `avaje-metrics-otel-reporter` for the scheduled reporter path
- `avaje-metrics-otel-producer` for the SDK `MetricProducer` path

## Maven dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics-otel-trace</artifactId>
  <version>9.9-RC5</version>
</dependency>
```

## Basic usage

No explicit setup is required in this module beyond having OpenTelemetry configured globally.
The span factory is discovered via `ServiceLoader`.

```java
Timer timer = Metrics.tracedTimer("app.service.method");

timer.time(() -> "ok");
```

When a traced timer includes a `label:...` tag, that label is used as the span name while the
aggregated metric name remains available via the `avaje.metrics.name` attribute.

`Timer.Event.endWithError()` marks the span as an error, and
`Timer.Event.endWithError(Throwable)` also records the exception on the span.

## Using with the OTEL Java agent

This module is especially convenient with the OpenTelemetry Java agent because the agent installs
the global `OpenTelemetry` instance used by traced timers.
