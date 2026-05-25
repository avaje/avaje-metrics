# Guide: Add OpenTelemetry Export

## Purpose

This guide provides step-by-step instructions for exporting **avaje-metrics** data to
OpenTelemetry.

When asked to *"export metrics to OpenTelemetry"*, *"choose the right OTEL module"*, or
*"wire avaje-metrics into OpenTelemetry"* in a project, follow these steps exactly.

---

## Overview

The main decision is **which OpenTelemetry module to use**.

| Module | Use when |
|---|---|
| `avaje-metrics-otel` | you want the easiest OTLP-backed setup for avaje metrics and traced timers |
| `avaje-metrics-otel-producer` | you already own the OpenTelemetry SDK wiring and want collection driven by the SDK reader/exporter |
| `avaje-metrics-otel-trace` | you only want traced timers / spans and are not exporting avaje metrics via OTEL |
| `avaje-metrics-otel-reporter` | you explicitly want the scheduled reporter path rather than the SDK `MetricProducer` path |

In most new setups, start with **`avaje-metrics-otel`** unless you have a clear reason to
own the SDK wiring yourself.

---

## Step 1 — Choose the module

### Easiest path: `avaje-metrics-otel`

Use this when you want:

- OTLP-backed metrics export
- OTLP-backed trace export
- `avaje-metrics-otel-producer` registered automatically
- traced timers working out of the box

### Explicit SDK path: `avaje-metrics-otel-producer`

Use this when:

- the application already builds its own `SdkMeterProvider`
- collection should be driven by the OpenTelemetry reader/exporter
- you want avaje metrics collected alongside normal OpenTelemetry metrics

### Traced timers only: `avaje-metrics-otel-trace`

Use this when:

- the goal is spans from `buildTraced()` or `@Timed(span = ON)`
- avaje metrics are exported some other way, or not exported through OTEL at all

### Scheduled reporter path: `avaje-metrics-otel-reporter`

Use this when:

- you explicitly want avaje-metrics to run its own reporting schedule
- you do not want the `MetricProducer` bridge

Do **not** use `avaje-metrics-otel-reporter` and `avaje-metrics-otel-producer` for the
same registry/export path at the same time.

---

## Step 2 — Start with the convenience module when possible

Add the dependency:

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics-otel</artifactId>
  <version>${version}</version>
</dependency>
```

Minimal setup:

```java
import io.avaje.metrics.otel.MetricsOpenTelemetry;

import java.time.Duration;

var openTelemetry = MetricsOpenTelemetry.builder()
  .endpoint("http://otel-collector:4317")
  .serviceName("my-service")
  .meterInterval(Duration.ofSeconds(60))
  .traceInterval(Duration.ofSeconds(10))
  .buildAndRegisterGlobal();
```

This is the best default when the application wants both OTEL metrics export and traced
timer support with minimal setup code.

When using this we do **NOT** need Step 3 or Step 4.

---

## Step 3 — Use `avaje-metrics-otel-producer` when you already own the SDK

Add the dependency:

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics-otel-producer</artifactId>
  <version>${version}</version>
</dependency>
```

Then register the producer with your `SdkMeterProvider`:

```java
import io.avaje.metrics.Metrics;
import io.avaje.metrics.otel.producer.OtelMetricProducer;

var meterProvider = SdkMeterProvider.builder()
  .registerMetricReader(reader)
  .registerMetricProducer(
    OtelMetricProducer.builder()
      .registry(Metrics.registry())
      .timedThresholdMicros(1_000)
      .build())
  .build();
```

Use this path when the application already builds its own OpenTelemetry SDK and wants
collection intervals controlled by the SDK reader/exporter.

---

## Step 4 — Add traced timers only when metric export is handled elsewhere

Add the dependency:

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics-otel-trace</artifactId>
  <version>${version}</version>
</dependency>
```

Then use traced timers:

```java
var timer = Metrics.timerBuilder("app.service.method")
  .buildTraced();

timer.time(service::run);
```

This module does not export avaje metrics to OTEL backends. It only provides the span
bridge for traced timers.

---

## Step 5 — Verify

1. Start the application with the chosen OTEL path configured.
2. Record some avaje metrics:

```java
Metrics.counter("app.requests").inc();
Metrics.timer("app.service.run").time(service::run);
```

3. Confirm the metrics and, if enabled, traced timer spans arrive in the configured
OpenTelemetry backend or collector.

## Notes

- `avaje-metrics-otel` is the best default for new OTLP-backed setups.
- `avaje-metrics-otel-producer` is the right choice when the application already owns the SDK.
- `avaje-metrics-otel-trace` is trace-only.
- `avaje-metrics-otel-reporter` is a separate scheduled path and should be used intentionally.
- For full builder and mapping details, see the module READMEs:
  - [metrics-otel/README.md](../../metrics-otel/README.md)
  - [metrics-otel-producer/README.md](../../metrics-otel-producer/README.md)
  - [metrics-otel-trace/README.md](../../metrics-otel-trace/README.md)
  - [metrics-otel-reporter/README.md](../../metrics-otel-reporter/README.md)
