# Guide: Register JVM Metrics

## Purpose

This guide provides step-by-step instructions for registering the built-in JVM metric
sets exposed by **avaje-metrics**.

When asked to *"add JVM metrics"*, *"register runtime metrics"*, *"add heap/thread/GC
metrics"*, or *"use `Metrics.jvmMetrics()`"* in a project, follow these steps exactly.

---

## Overview

avaje-metrics exposes built-in JVM metrics via `Metrics.jvmMetrics()` and `MetricRegistry`
because `MetricRegistry` extends `JvmMetrics`.

The most common choices are:

| Call | Best for |
|---|---|
| `registerJvmCoreMetrics()` | lower-cardinality / lower-cost baseline metrics |
| `registerJvmMetrics()` | full built-in JVM metric set |
| individual `register...()` methods | precise control over which JVM metrics are enabled |

---

## Step 1 — Choose core or full JVM metrics

For a smaller baseline set:

```java
import io.avaje.metrics.Metrics;

Metrics.jvmMetrics()
  .registerJvmCoreMetrics();
```

For the full default built-in set:

```java
Metrics.jvmMetrics()
  .registerJvmMetrics();
```

`registerJvmCoreMetrics()` is the better default when you want lower-cardinality export or using GraalVM native image.
`registerJvmMetrics()` is the better choice when broader runtime visibility matters more.

---

## Step 2 — Add detail and global tags when needed

```java
import io.avaje.metrics.Metrics;
import io.avaje.metrics.Tags;

Metrics.jvmMetrics()
  .withDetails()
  .withGlobalTags(Tags.of("env:dev", "service:billing"))
  .withReportAlways()
  .registerJvmMetrics();
```

Useful options:

- `withDetails()` — include more detailed GC, thread, and cgroup metrics where supported
- `withGlobalTags(...)` — apply stable tags such as environment or service name
- `withReportAlways()` — report even metrics that do not change often
- `withReportChangesOnly()` — reduce repeated reporting for stable values

---

## Step 3 — Register only selected JVM metric groups

If full registration is too broad, enable only the groups you need:

```java
Metrics.jvmMetrics()
  .withDetails()
  .registerJvmMemoryMetrics()
  .registerJvmThreadMetrics()
  .registerJvmGCMetrics();
```

Other focused options include:

- `registerJvmOsLoadMetric()`
- `registerProcessMemoryMetrics()`
- `registerCGroupMetrics()`

This is useful when export cost or metric volume matters.

---

## Step 4 — Verify

After registration, collect metrics from the default registry:

```java
var metrics = Metrics.collectMetrics();
var json = Metrics.collectAsJson().asJson();
```

Look for names such as:

- `jvm.memory.heap.used`
- `jvm.threads.current`
- `jvm.gc.time`

---

## Notes

- `registerJvmCoreMetrics()` is usually the safest starting point for production export.
- `withDetails()` increases metric volume; use it intentionally.
- `withGlobalTags(...)` is a good place to apply stable tags such as environment or service.
- The built-in JVM metrics can be registered on the default registry or any custom
  `MetricRegistry`.
