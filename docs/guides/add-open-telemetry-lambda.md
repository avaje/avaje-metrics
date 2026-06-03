# Guide: Add OpenTelemetry Export — AWS Lambda

## Purpose

This guide provides step-by-step instructions for exporting **avaje-metrics** data
(and related traces) to OpenTelemetry from an **AWS Lambda** function (or any similar
"freeze-on-exit" serverless runtime).

When asked to *"export Lambda metrics to OpenTelemetry"*, *"why are my Lambda metrics
missing in Grafana / Mimir / Tempo"*, or *"add `enableWaitIfRunning()` to a Lambda
function"*, follow these steps exactly.

This guide is the Lambda-specific companion to
[add-open-telemetry-export.md](add-open-telemetry-export.md). Read that one first if
you have not yet decided which OTEL module to use.

---

## Overview

AWS Lambda freezes the worker between invocations. Two consequences matter for
OpenTelemetry:

1. **Freeze-on-exit cuts in-flight exports mid-flight.** The default
   `PeriodicMetricReader` and `BatchSpanProcessor` background threads ship telemetry
   asynchronously. If the runtime suspends while an OTLP HTTP/gRPC export is in
   progress, the request is interrupted. The export *may* complete on a later thaw,
   minutes later — or be lost.

2. **Low-traffic Lambdas starve the periodic reader.** A Lambda invoked once a
   minute spends most of its life frozen. The periodic reader cannot tick on a
   reliable schedule, so metrics produced inside an invocation may never be exported
   before the runtime is suspended again.

`avaje-metrics-otel` solves both with a single builder call:

```java
.enableWaitIfRunning()
```

This wraps the metric and span exporters to track in-flight exports and provides a
`TelemetryWaiter` that, at the **end** of an invocation:

1. **Waits** briefly if a scheduled background export is currently in progress, and
2. **Force-flushes** telemetry that has gone stale (no successful background export
   within the configured `flushIfStale` window).

In busy environments the periodic reader keeps `lastSuccess` fresh and the stale
forceFlush is a no-op. In low-traffic environments the stale forceFlush ships data on
the invocation thread before the runtime is suspended again.

The pattern mirrors the avaje-metrics StatsD `waitIfRunning()` pattern: most
invocations have zero overhead; only an invocation that overlaps an active export, or
arrives after a long quiet period, pays a brief synchronous cost.

---

## Step 1 — Add the dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics-otel</artifactId>
  <version>${version}</version>
</dependency>
```

This is the same module as the standard OTEL recipe — Lambda support is built in.

---

## Step 2 — Build the SDK once at handler-class init

Build the `OpenTelemetrySdk` and `TelemetryWaiter` **once** when the Lambda handler
class is loaded — not per invocation. The Lambda runtime reuses the same handler
instance across invocations on the same warm worker.

```java
import io.avaje.metrics.otel.MetricsOpenTelemetry;
import io.avaje.metrics.otel.TelemetryWaiter;
import io.opentelemetry.sdk.OpenTelemetrySdk;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class OrdersHandler {

  private static final TelemetryWaiter WAITER;

  static {
    var result = MetricsOpenTelemetry.builder()
        .protocol(MetricsOpenTelemetry.Protocol.HTTP_PROTOBUF)
        .endpoint(System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT"))
        .serviceName("orders-lambda")
        .deploymentEnvironmentName(System.getenv("ENV"))
        .exportTimeout(Duration.ofSeconds(5))
        .connectTimeout(Duration.ofSeconds(2))
        .enableWaitIfRunning()
            .timeout(35, TimeUnit.SECONDS)
            // .flushIfStale(Duration.ofSeconds(60))  // optional
            .buildAndRegisterGlobal();

    WAITER = result.waiter();
  }

  public Response handle(Request request) {
    try {
      return process(request);
    } finally {
      WAITER.waitIfRunning();
    }
  }
}
```

The two critical pieces are:

- `enableWaitIfRunning()` — wraps the exporters and produces the waiter.
- `WAITER.waitIfRunning()` in a `finally` block — runs at the end of every
  invocation, blocks briefly only if needed.

If you have multiple Lambda handler classes in the same deployment artifact (e.g. an
event handler **and** a schedule handler), build the SDK in a shared class so all
handlers use the same `TelemetryWaiter` instance.

---

## Step 3 — Understand the Lambda-friendly defaults

Calling `enableWaitIfRunning()` switches two defaults to Lambda-friendly values, but
only when the caller has not already set them explicitly:

| Setting              | Standalone default | Lambda default               | Why |
|----------------------|--------------------|------------------------------|-----|
| `meterInterval`      | 60 seconds         | **30 seconds**               | Faster ticks reduce stale-flush frequency in low-traffic envs. |
| `flushIfStale`       | n/a                | **2 × meterInterval** (60 s) | Forgives one missed tick + jitter; flushes when two or more are missed. |
| `WaiterBuilder.timeout` | 5 seconds       | 5 seconds (set higher)       | Lambda freezes mid-export are common — recommend **35 s**. |
| `connectTimeout`     | unset (10 s SDK)   | recommend **2 s**            | Fail fast on networking issues so they don't burn invocation time. |
| `exportTimeout`      | unset (10 s SDK)   | recommend **5 s**            | Same. |

You can override any of these. Pass `Duration.ZERO` to `flushIfStale(...)` to disable
the stale forceFlush and use only the in-flight wait behaviour.

### How `flushIfStale` works

The waiter records `lastSuccessAtMillis` whenever a wrapped export completes
successfully. At the end of `waitIfRunning()`:

1. If `(now - lastSuccess) > flushIfStale` for metrics, call `SdkMeterProvider.forceFlush()`.
2. If `(now - lastSuccess) > flushIfStale` for spans, call `SdkTracerProvider.forceFlush()`.

`lastSuccess` starts at `0`, so the **first invocation after cold start** always
triggers a forceFlush — this is intentional, and ships startup metrics promptly.

---

## Step 4 — Wiring with dependency injection

> **⚠ Ordering matters.** The `@Bean` method that returns `TelemetryWaiter` must
> declare `OpenTelemetry` as a parameter so the DI container invokes
> `openTelemetry()` first. Without that parameter, the waiter bean may be created
> before the SDK is built, returning a no-op waiter that silently never flushes.

### Spring

```java
@Configuration
public class MetricsConfig {

  @Bean
  OpenTelemetry openTelemetry() {
    var result = MetricsOpenTelemetry.builder()
        .endpoint(System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT"))
        .protocol(MetricsOpenTelemetry.Protocol.HTTP_PROTOBUF)
        .serviceName("orders-lambda")
        .exportTimeout(Duration.ofSeconds(5))
        .enableWaitIfRunning()
            .timeout(35, TimeUnit.SECONDS)
            .buildAndRegisterGlobal();
    this.waiter = result.waiter();
    return result.sdk();
  }

  @Bean
  TelemetryWaiter telemetryWaiter(OpenTelemetry openTelemetry) {
    return waiter;   // captured above; OpenTelemetry param forces this to run after openTelemetry()
  }

  private TelemetryWaiter waiter;
}
```

### avaje-inject

```java
@Factory
public class MetricsConfig {

  private TelemetryWaiter waiter;

  @Bean
  OpenTelemetry openTelemetry() {
    var result = MetricsOpenTelemetry.builder()
        .endpoint(Config.getNullable("otel.endpoint"))
        .protocol(MetricsOpenTelemetry.Protocol.HTTP_PROTOBUF)
        .serviceName("orders-lambda")
        .exportTimeout(Duration.ofSeconds(5))
        .enableWaitIfRunning()
            .timeout(Config.getInt("otel.waitTimeoutMillis", 35_000), TimeUnit.MILLISECONDS)
            .buildAndRegisterGlobal();
    this.waiter = result.waiter();
    return result.sdk();
  }

  @Bean
  TelemetryWaiter telemetryWaiter(OpenTelemetry openTelemetry) {
    return waiter;   // OpenTelemetry param forces this to run after openTelemetry()
  }
}
```

In the handler:

```java
public class OrdersLambda {

  private final TelemetryWaiter waiter;
  // ... other deps

  public Response handle(Request request) {
    try {
      return process(request);
    } finally {
      waiter.waitIfRunning();
    }
  }
}
```

When OTEL is disabled (e.g. in a local dev profile that does not configure an
endpoint), inject `TelemetryWaiter.noop()` instead — the same handler code keeps
working with zero overhead.

---

## Step 5 — Verify

Enable DEBUG logging on the `io.avaje.metrics.otel` logger (or your application's
matching package) so you can see the export lifecycle. With `avaje-simple-logger`:

```properties
log.level.io.avaje.metrics.otel=DEBUG
```

You should see a sequence like this on a healthy warm invocation:

```
DEBUG io.avaje.metrics.otel - OTLP metric export starting count:90
DEBUG io.avaje.metrics.otel - OTLP metric export completed count:90 elapsedMs:219
```

On the first invocation after cold start (or after a quiet period):

```
DEBUG io.avaje.metrics.otel - OTLP metric forceFlush triggered (stale)
DEBUG io.avaje.metrics.otel - OTLP metric forceFlush completed elapsedMs:585
```

If `waitIfRunning()` had to wait for an in-flight tick:

```
DEBUG io.avaje.metrics.otel - Waiting up to 35000ms for in-flight OTLP metric export
```

Failures and timeouts are logged at WARN:

```
WARN  io.avaje.metrics.otel - OTLP metric export failed count:90 elapsedMs:5001 ...
WARN  io.avaje.metrics.otel - Timed out waiting 35000ms for OpenTelemetry metric export to complete
```

---

## Notes

### Diagnostics

- **No metrics in dashboard, no logs about exports**
  Check the OTEL endpoint is reachable from the Lambda (VPC/security groups). The
  handler will time out if `exportTimeout` is unset — set it explicitly to a value
  smaller than the Lambda timeout.

- **`Timed out waiting` lines**
  Bump `WaiterBuilder.timeout(...)`. 35 seconds is a good starting point for Lambdas
  configured with a 60-second timeout.

- **Cold-start metrics arrive but warm-invocation metrics are missing**
  The periodic reader is starved — set or shorten `flushIfStale` (default already
  60 s with the Lambda preset).

- **Multi-minute completion latencies**
  Almost always the freeze-on-exit problem. The fix is exactly this guide.

### Cold-start cost

Building `MetricsOpenTelemetry` and the SDK in `static` initializer or DI factory adds
to cold-start time. Typical cold start cost is in the order of 50-200 ms depending on
the configured exporters.

### Tradeoffs

- `flushIfStale` adds a synchronous export to the **first invocation after cold
  start** (because `lastSuccessAtMillis` starts at 0). This is usually desirable —
  startup metrics ship promptly — but it does add a few hundred ms to the first
  warm-up invocation. Pass `Duration.ZERO` to disable.
- `connectTimeout` and `exportTimeout` are passed through to the default OTLP
  HTTP/gRPC exporters only. They have no effect when a custom exporter is supplied
  via `metricExporter(...)` or `spanExporter(...)`.
- `TelemetryWaiter.noop()` is safe to use as a fallback when OTEL is disabled — it
  performs no waiting and no flushing.
