# avaje-metrics-otel-producer

Exposes [avaje-metrics](https://avaje-metrics.github.io) to OpenTelemetry using the SDK-side
`MetricProducer` bridge.

Unlike `avaje-metrics-otel`, this module does not run its own reporting schedule. Metric collection
happens when the OpenTelemetry SDK reader/exporter collects, so interval boundaries align with the
configured OTLP export interval or Prometheus scrape/reader interval.

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

```java
SdkMeterProvider meterProvider = SdkMeterProvider.builder()
    .registerMetricReader(reader)
    .registerMetricProducer(
        OtelMetricProducer.builder()
            .registry(Metrics.registry())
            .timedThresholdMicros(1_000)
            .build())
    .build();
```

## Builder options

```java
OtelMetricProducer producer = OtelMetricProducer.builder()

    // Use a non-default MetricRegistry (defaults to Metrics.registry())
    .registry(myRegistry)

    // Skip timer metrics whose total duration is below this threshold.
    // 1_000 = skip timers with less than 1ms total execution per collection interval.
    .timedThresholdMicros(1_000)

    // OpenTelemetry instrumentation scope name (default: "io.avaje.metrics")
    .scopeName("io.avaje.metrics")

    .build();
```

## Metric mapping

avaje metrics are mapped to OTEL metric data as follows:

| avaje type | OTEL metric data | Notes |
|---|---|---|
| `Counter` | delta `LongSum` | monotonic, unit `{event}` |
| `Timer` | delta `LongSum` (`name.count`), delta `LongSum` (`name.total`), `LongGauge` (`name.max`) | values in microseconds (`us`) |
| `Meter` | delta `LongSum` (`name.count`), delta `LongSum` (`name.total`), `LongGauge` (`name.max`) | values in user-defined units |
| `GaugeLong` | `LongGauge` | current value at collection time |
| `GaugeDouble` | `DoubleGauge` | current value at collection time |

Timer success and error statistics are exported separately as `name` and `name.error`, matching the
existing avaje timer reporting behavior.

## When to use this module

Use `avaje-metrics-otel-producer` when you want collection-time intervals controlled by the
OpenTelemetry SDK reader/exporter, for example:

- OTLP export intervals should define the delta window
- Prometheus / pull-style collection should define the interval
- You want to avoid a separate avaje reporting scheduler

Use `avaje-metrics-otel` when you want the lighter API-only scheduled reporter and do not need a
`MetricProducer`.

## Important limitation

`MetricProducer.produce(...)` returns metrics produced since the previous collection. That makes
this bridge effectively a single-reader solution for a given avaje registry/export path. Do not
register multiple readers against the same `OtelMetricProducer` unless you are prepared for one
reader to drain the interval before another reads it.

Do not use `avaje-metrics-otel` and `avaje-metrics-otel-producer` for the same registry/export
path at the same time, or you will emit duplicate telemetry.
