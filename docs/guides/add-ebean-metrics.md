# Guide: Add Ebean Metrics

## Purpose

This guide provides step-by-step instructions for exposing Ebean database metrics through
**avaje-metrics** using `avaje-metrics-ebean`.

When asked to *"add Ebean metrics"*, *"collect database metrics from Ebean"*, or *"use
`DatabaseMetricSupplier`"* in a project, follow these steps exactly.

---

## Overview

`avaje-metrics-ebean` supplies Ebean database metrics into avaje-metrics via
`DatabaseMetricSupplier`.

Once the supplier is added, the database metrics participate in the normal avaje-metrics
collection/export paths such as:

- `Metrics.collectMetrics()`
- `Metrics.collectAsJson()`
- registry-based OpenTelemetry export such as `avaje-metrics-otel-producer`

---

## Step 1 — Add the dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics-ebean</artifactId>
  <version>${version}</version>
</dependency>
```

If the project uses `module-info.java`, also add:

```java
requires io.avaje.metrics.ebean;
```

---

## Step 2 — Add `DatabaseMetricSupplier`

```java
import io.avaje.metrics.Metrics;
import io.avaje.metrics.ebean.DatabaseMetricSupplier;

Metrics.addSupplier(new DatabaseMetricSupplier(database));
```

That is the core setup. After this, Ebean metrics are part of normal avaje-metrics
collection.

---

## Step 3 — Collect or export the metrics

Manual collection:

```java
var metrics = Metrics.collectMetrics();
var json = Metrics.collectAsJson().asJson();
```

Registry-based export paths such as OpenTelemetry producer can use the same registry once
the supplier has been added.

---

## Step 4 — Understand delta vs cumulative collection

`DatabaseMetricSupplier` supports both delta and cumulative collection modes.

Examples:

```java
var deltaMetrics = Metrics.collectMetrics();
var cumulativeMetrics = Metrics.collectMetrics(CollectionMode.CUMULATIVE);
```

Use delta collection when you want per-interval reporting behavior. Use cumulative
collection when your export path expects lifetime-accumulating counts/totals.

---

## Step 5 — Prefer direct reporter integration when available

If the application is using `avaje-metrics-statsd` or `avaje-metrics-graphite`, those
modules can include database metrics directly from their builder API:

```java
StatsdReporter.builder()
  .database(database)
  .build()
  .start();
```

and:

```java
var reporter = GraphiteReporter.builder()
  .database(database)
  .build();

reporter.report();
```

In those cases, manual supplier registration may not be necessary.

## Notes

- `DatabaseMetricSupplier` maps Ebean timed metrics, query metrics, and count metrics
  into avaje-metrics `TimerStats` and `CounterStats`.
- This module is most useful when the export path is built around registry collection.
- For reporter modules with direct `.database(...)` support, use the simpler direct path
  unless you specifically need supplier-level control.
- `avaje-metrics-ebean` collects database metrics. It is separate from
  `ebean-opentelemetry`, which creates Ebean transaction spans. When using
  `ebean-opentelemetry`, register the global OpenTelemetry instance before Ebean
  `Database` beans are built.
