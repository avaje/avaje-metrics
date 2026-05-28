# avaje-metrics-otel-trace

Provides the optional OpenTelemetry span bridge used by traced timers.

When this module is on the classpath or module path,
`Metrics.timerBuilder(...).buildTraced()`, `Metrics.timerBuilder(...).buildRootTraced()`,
and enhancement with `@Timed(span = Timed.SpanMode.CHILD)` or
`@Timed(span = Timed.SpanMode.ROOT)` create OpenTelemetry spans via `GlobalOpenTelemetry`.

This module does **not** export avaje metrics to OpenTelemetry metrics backends. For that use:

- `avaje-metrics-otel` for the OTLP-backed convenience helper path
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
Timer timer = Metrics.timerBuilder("app.service.method")
  .buildTraced();

timer.time(() -> "ok");
```

```java
Timer timer = Metrics.timerBuilder("app.service.method")
  .tags(Tags.of("env:prod"))
  .bucketRanges(50, 100, 250)
  .buildTraced();
```

Use `buildRootTraced()` or `@Timed(span = Timed.SpanMode.ROOT)` on a top-level boundary
that should start a root span when no recording span is current. Root traced timers use
the default OpenTelemetry `INTERNAL` span kind and make the span current for the timed
operation.

When a traced timer includes a `label:...` tag, that label is used as the span name while the
aggregated metric name remains available via the `avaje.metrics.name` attribute.

`Timer.Event.endWithError()` marks the span as an error, and
`Timer.Event.endWithError(Throwable)` also records the exception on the span.

## Using with the OTEL Java agent

This module is especially convenient with the OpenTelemetry Java agent because the agent installs
the global `OpenTelemetry` instance used by traced timers.
