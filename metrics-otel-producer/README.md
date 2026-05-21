# avaje-metrics-otel-producer

Exposes [avaje-metrics](https://avaje-metrics.github.io) to OpenTelemetry using the SDK-side
`MetricProducer` bridge.

Unlike `avaje-metrics-otel`, this module does not run its own reporting schedule. Metric collection
happens when the OpenTelemetry SDK reader/exporter collects.

- counters, `name.count`, and `name.total` are exported as cumulative monotonic sums
- `name.max` is exported as a gauge and resets on each collection

## Maven dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics-otel-producer</artifactId>
  <version>9.9-RC3</version>
</dependency>
```

This module depends on the OpenTelemetry SDK metrics bridge types, so it is intended for SDK-based
setups rather than API-only setups.

## Basic usage

The `MetricProducer` is registered with an OpenTelemetry SDK `SdkMeterProvider`. The
`registerMetricReader(...)` call is where you choose how metrics are actually collected/exported.
Common reader choices are:

- `PeriodicMetricReader` for OTLP push/export
- Prometheus reader/server types for Prometheus scraping

Any normal OpenTelemetry metrics created from the same `SdkMeterProvider` are collected alongside
the avaje metrics exposed by `OtelMetricProducer`.

### Example: OTLP push

```java
MetricReader reader =
    PeriodicMetricReader.builder(
            OtlpGrpcMetricExporter.builder()
                .setEndpoint("http://otel-collector:4317")
                .build())
        .setInterval(Duration.ofSeconds(60))
        .build();

SdkMeterProvider meterProvider = SdkMeterProvider.builder()
    .registerMetricReader(reader)
    .registerMetricProducer(
        OtelMetricProducer.builder()
            .registry(Metrics.registry())
            .timedThresholdMicros(1_000)
            .build())
    .build();

OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
    .setMeterProvider(meterProvider)
    .build();

// Normal OpenTelemetry meters are collected too
Meter meter = openTelemetry.getMeter("my.app");
LongCounter counter = meter.counterBuilder("my.app.requests").build();
counter.add(1);
```

### Example: Prometheus

```java
PrometheusHttpServer prometheus = PrometheusHttpServer.builder()
    .setHost("0.0.0.0")
    .setPort(9464)
    .build();

SdkMeterProvider meterProvider = SdkMeterProvider.builder()
    .registerMetricReader(prometheus)
    .registerMetricProducer(
        OtelMetricProducer.builder()
            .registry(Metrics.registry())
            .build())
    .build();
```

## Builder options

```java
OtelMetricProducer producer = OtelMetricProducer.builder()

    // Use a non-default MetricRegistry (defaults to Metrics.registry())
    .registry(myRegistry)

    // Skip timer metrics whose cumulative total duration is below this threshold.
    // 1_000 = skip timers with less than 1ms cumulative execution.
    .timedThresholdMicros(1_000)

    // OpenTelemetry instrumentation scope name (default: "io.avaje.metrics")
    .scopeName("io.avaje.metrics")

    .build();
```

## Metric mapping

avaje metrics are mapped to OTEL metric data as follows:

| avaje type | OTEL metric data | Notes |
|---|---|---|
| `Counter` | cumulative `LongSum` | monotonic, unit `{event}` |
| `Timer` | cumulative `LongSum` (`name.count`), cumulative `LongSum` (`name.total`), `LongGauge` (`name.max`) | count/total are cumulative; `max` resets each collection; values in microseconds (`us`) |
| `Meter` | cumulative `LongSum` (`name.count`), cumulative `LongSum` (`name.total`), `LongGauge` (`name.max`) | count/total are cumulative; `max` resets each collection |
| `GaugeLong` | `LongGauge` | current value at collection time |
| `GaugeDouble` | `DoubleGauge` | current value at collection time |

Timer success and error statistics are exported separately as `name` and `name.error`, matching the
existing avaje timer reporting behavior.

## When to use this module

Use `avaje-metrics-otel-producer` when you want collection-time intervals controlled by the
OpenTelemetry SDK reader/exporter, for example:

- you want Prometheus / Grafana-friendly cumulative sums for count / total
- OTLP export or Prometheus / pull-style collection should drive when metrics are read
- you want `name.max` to reflect the current collection window
- You want to avoid a separate avaje reporting scheduler

Use `avaje-metrics-otel` when you want the lighter API-only scheduled reporter and do not need a
`MetricProducer`.

If you want a convenience module that builds an OTLP-backed `OpenTelemetrySdk` and registers
`OtelMetricProducer` for you, use `avaje-metrics-otel-otlp`.

## Important limitation

This bridge should still be treated as a single-reader solution for a given avaje registry/export
path. Although counters and totals are cumulative, `name.max` resets on collection, so one reader
can consume the max window before another reads it.

Supplier-backed metrics only behave cumulatively when the supplier implements
`MetricSupplier.collectMetrics(CollectionMode)`.

`avaje-metrics-ebean` now supports both delta and cumulative collection modes. Its cumulative timed
and query `max` values rely on the corresponding Ebean runtime fix that resets `max` on each
collection; without that runtime change, cumulative `count` and `total` still work but `max`
behaves like a lifetime high-water mark.

Do not use `avaje-metrics-otel` and `avaje-metrics-otel-producer` for the same registry/export
path at the same time, or you will emit duplicate telemetry.
