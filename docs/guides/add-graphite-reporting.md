# Guide: Add Graphite Reporting

## Purpose

This guide provides step-by-step instructions for exporting **avaje-metrics** data to
Graphite using `avaje-metrics-graphite`.

When asked to *"add Graphite reporting"*, *"send metrics to Graphite"*, or *"configure
`GraphiteReporter`"* in a project, follow these steps exactly.

---

## Overview

`avaje-metrics-graphite` reports avaje-metrics data to a Carbon / Graphite server.

The usual pattern is:

1. add `avaje-metrics-graphite`
2. build a `GraphiteReporter`
3. call `report()` on the schedule owned by the application
4. close or stop that schedule during application shutdown

---

## Step 1 - Add the dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics-graphite</artifactId>
  <version>${version}</version>
</dependency>
```

If the application uses `module-info.java`, also add:

```java
requires io.avaje.metrics.graphite;
```

---

## Step 2 - Build the reporter

```java
import io.avaje.metrics.graphite.GraphiteReporter;

GraphiteReporter reporter = GraphiteReporter.builder()
  .prefix("prod.billing.")
  .hostname("graphite.example.com")
  .port(2003)
  .timedThresholdMicros(1_000)
  .build();
```

Common builder options:

- `prefix(...)` prepends a common metric path prefix such as environment and service.
- `hostname(...)` and `port(...)` set the Graphite destination.
- `socketFactory(...)` supplies a custom socket factory when needed.
- `batchSize(...)` tunes the number of tuples per Graphite payload.
- `timedThresholdMicros(...)` suppresses low-value timer metrics.
- `excludeDefaultRegistry()` reports only explicitly added registries or suppliers.

---

## Step 3 - Schedule reporting

`GraphiteReporter` does not own a scheduler. Call `report()` from application-owned
scheduling infrastructure:

```java
var executor = Executors.newSingleThreadScheduledExecutor();
executor.scheduleAtFixedRate(reporter::report, 60, 60, TimeUnit.SECONDS);
```

Close the scheduler on shutdown:

```java
executor.shutdown();
```

---

## Step 4 - Add custom registries or database metrics when needed

Use a non-default registry:

```java
var registry = Metrics.createRegistry();

GraphiteReporter reporter = GraphiteReporter.builder()
  .registry(registry)
  .hostname("graphite.example.com")
  .port(2003)
  .build();
```

Include Ebean database metrics directly:

```java
GraphiteReporter reporter = GraphiteReporter.builder()
  .database(database)
  .hostname("graphite.example.com")
  .port(2003)
  .build();
```

Use `registry(MetricSupplier)` when a custom supplier exposes metrics from another
source.

---

## Step 5 - Understand label-tag metric names

Graphite metric paths do not carry tags. When a metric has a `label:<value>` tag,
the reporter appends the label value to the Graphite metric path.

For example:

```text
web.api + label:MyController.myMethod
```

is reported as:

```text
web.api.MyController.myMethod.count
web.api.MyController.myMethod.total
web.api.MyController.myMethod.mean
web.api.MyController.myMethod.max
```

For compatibility with older application timer names, the `app.component` base name
uses the legacy `app.<label>` path:

```text
app.component + label:MyClass.myMethod -> app.MyClass.myMethod.count
```

Non-label tags are ignored by the Graphite reporter because the current Graphite
path format has no tag dimension.

---

## Step 6 - Verify

1. Start the application with the reporter scheduled.
2. Record a few metrics:

```java
Metrics.counter("app.requests").inc();
Metrics.timer("app.service.run").time(service::run);
```

3. Confirm the expected metric paths arrive in Graphite.

## Notes

- `GraphiteReporter` reports the default registry unless `excludeDefaultRegistry()` is used.
- `prefix(...)` should usually include a trailing period, such as `prod.billing.`.
- `timedThresholdMicros(...)` is useful for broad `@Timed` enhancement where many methods
  are not operationally interesting.
- `database(...)`, `registry(...)`, and `registry(MetricSupplier)` can be combined when
  multiple metric sources need to be exported.
