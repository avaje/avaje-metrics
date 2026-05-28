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
| `@Timed` | declarative timing via build-time enhancement |
| `Timer.time(...)` or `Timer.startEvent()` | explicit programmatic timing in code |
| `Metrics.timerBuilder(...).buildTraced()` | timing plus child spans when trace support is present |
| `Metrics.timerBuilder(...).buildRootTraced()` | timing plus root-if-needed spans when trace support is present |

If you do **not** want enhancement, programmatic timers are the safest and most explicit
path. If you want `@Timed`, configure build-time enhancement first.

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

Then build a child traced timer:

```java
var tracedTimer = Metrics.timerBuilder("app.service.run")
  .tags(Tags.of("operation:sync"))
  .buildTraced();

tracedTimer.time(service::run);
```

`buildTraced()` creates child spans when trace support is available, the application has a
global `OpenTelemetry` instance, and there is a current recording span.

Use `buildRootTraced()` for a top-level boundary that should start a root span when no
recording span is current:

```java
var rootTimer = Metrics.timerBuilder("app.lambda.handle")
  .buildRootTraced();

rootTimer.time(handler::handleRequest);
```

---

## Step 4 — Use `@Timed` with build-time enhancement

Before relying on `@Timed`, enable build-time enhancement with
`metrics-maven-plugin` and configure it with `metrics.mf`. See
[configure-metrics-agent.md](configure-metrics-agent.md) for the Maven plugin setup,
`metrics.mf` options, naming modes, and troubleshooting.

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

Add stable custom tags when the enhanced timer should carry dimensions:

```java
@Timed(tags = {"component:billing", "marker:blue"})
class BillingService {

  @Timed(tags = "operation:sync")
  void syncInvoices() {
    // tags: component:billing, marker:blue, operation:sync
  }
}
```

Tags use the same `key:value` format as `Tags.of(...)`. Class-level tags apply to each
timed method and method-level tags append to them. These tags are part of the enhanced
static timer setup, so use stable low-cardinality values rather than request-specific data.

You can also enable spans for enhanced methods:

```java
@Timed(span = Timed.SpanMode.CHILD)
class BillingService {
  void syncInvoices() {
  }
}
```

Important behavior:

- class-level `@Timed` applies timing to public methods by default
- `@NotTimed` excludes specific methods
- method-level `@Timed` is useful for overrides or private methods
- `@Timed(tags = {...})` adds custom timer tags using `key:value` values
- `@Timed(span = Timed.SpanMode.CHILD)` requires trace support such as `avaje-metrics-otel-trace`
- `@Timed(span = Timed.SpanMode.ROOT)` starts a root span when no recording span is current

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
- Prefer `buildTraced()` when you want timing plus child spans for the same code path.
- Prefer `buildRootTraced()` or `@Timed(span = Timed.SpanMode.ROOT)` for top-level Lambda-style boundaries.
- `@Timed` is the declarative path when the application uses build-time enhancement.
- Timers support tags and bucket ranges via `Metrics.timerBuilder(...)` and `@Timed`.
