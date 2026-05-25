# Guide: Add Method Timing

## Purpose

This guide provides step-by-step instructions for adding method or code-block timing
with **avaje-metrics**.

When asked to *"time this method"*, *"add a timer"*, *"use `@Timed`"*, or *"add traced
timers"* in a project, follow these steps exactly.

---

## Overview

There are three main timing styles:

| Approach | Best for |
|---|---|
| `@Timed` | declarative timing when enhancement is already enabled in the application |
| `Timer.time(...)` or `Timer.startEvent()` | explicit programmatic timing in code |
| `Metrics.timerBuilder(...).buildTraced()` | timing plus spans when trace support is present |

If you do **not** already use enhancement, programmatic timers are the safest and most
explicit path.

---

## Step 1 — Add a programmatic timer

```java
import io.avaje.metrics.Metrics;
import io.avaje.metrics.Tags;

var timer = Metrics.timerBuilder("app.service.run")
  .tags(Tags.of("operation:sync"))
  .build();

timer.time(service::run);
```

This is the simplest way to time a code path and record success statistics.

---

## Step 2 — Use explicit event lifecycle when you need error handling

```java
var timer = Metrics.timer("app.service.run");

var event = timer.startEvent();
try {
  service.run();
  event.end();
} catch (RuntimeException e) {
  event.endWithError(e);
  throw e;
}
```

This path is useful when you want explicit control over success versus error timing.

For lower overhead, you can also time from `System.nanoTime()`:

```java
var timer = Metrics.timer("app.service.run");

long startNanos = System.nanoTime();
try {
  service.run();
  timer.add(startNanos);
} catch (RuntimeException e) {
  timer.addErr(startNanos);
  throw e;
}
```

---

## Step 3 — Use traced timers when spans are also needed

Add `avaje-metrics-otel-trace` when the project already has OpenTelemetry configured:

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics-otel-trace</artifactId>
  <version>${version}</version>
</dependency>
```

Then build a traced timer:

```java
var tracedTimer = Metrics.timerBuilder("app.service.run")
  .tags(Tags.of("operation:sync"))
  .buildTraced();

tracedTimer.time(service::run);
```

`buildTraced()` creates spans when trace support is available and the application has a
global `OpenTelemetry` instance.

---

## Step 4 — Use `@Timed` when enhancement is already enabled

```java
import io.avaje.metrics.annotation.NotTimed;
import io.avaje.metrics.annotation.Timed;

@Timed
class BillingService {

  void syncInvoices() {
    // timed
  }

  @NotTimed
  void helper() {
    // not timed
  }
}
```

You can also enable spans for enhanced methods:

```java
@Timed(span = Timed.SpanMode.ON)
class BillingService {
  void syncInvoices() {
  }
}
```

Important behavior:

- class-level `@Timed` applies timing to public methods by default
- `@NotTimed` excludes specific methods
- method-level `@Timed` is useful for overrides or private methods
- `@Timed(span = Timed.SpanMode.ON)` requires trace support such as `avaje-metrics-otel-trace`

---

## Step 5 — Verify

Call the timed method a few times, then inspect collected metrics:

```java
var metrics = Metrics.collectMetrics();
var json = Metrics.collectAsJson().asJson();
```

Expect to see timer statistics such as count, total, and max for the configured timer
name. Success and error timing are tracked separately.

## Notes

- Prefer programmatic timers when you want explicit behavior and no enhancement dependency.
- Prefer `buildTraced()` when you want timing plus spans for the same code path.
- `@Timed` is the declarative path when the application already uses enhancement.
- Timers support tags and bucket ranges via `Metrics.timerBuilder(...)`.
