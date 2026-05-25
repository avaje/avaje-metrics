# Guide: Add Prometheus Scraping

## Purpose

This guide provides step-by-step instructions for exposing **avaje-metrics** directly as
a Prometheus scrape endpoint using `avaje-metrics-prometheus`.

When asked to *"add Prometheus metrics"*, *"add a Prometheus scrape endpoint"*, or
*"export avaje-metrics in Prometheus format"* in a project, follow these steps exactly.

---

## Overview

`avaje-metrics-prometheus` is a lightweight pull exporter:

1. the application records metrics in a `MetricRegistry`
2. Prometheus scrapes an HTTP endpoint
3. the endpoint calls `PrometheusMetrics.scrape()`
4. avaje metrics are collected using `CollectionMode.CUMULATIVE`

Use this module when the application wants a direct Prometheus text endpoint without
building an OpenTelemetry SDK.

If the application already uses OpenTelemetry, use
[`avaje-metrics-otel-producer`](../../metrics-otel-producer/README.md) with the OTEL
Prometheus reader instead.

---

## Step 1 — Add the dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics-prometheus</artifactId>
  <version>${version}</version>
</dependency>
```

If the project uses `module-info.java`, also add:

```java
requires io.avaje.metrics.prometheus;
```

---

## Step 2 — Create the Prometheus scrape writer

```java
import io.avaje.metrics.Metrics;
import io.avaje.metrics.prometheus.PrometheusMetrics;

var prometheus = PrometheusMetrics.builder()
  .registry(Metrics.registry())
  .build();
```

Most applications use the default registry. Use `.registry(myRegistry)` only when the
application records metrics into a custom registry.

---

## Step 3 — Expose an HTTP endpoint

Expose a route such as `/metrics` from the application's existing web framework.

The response should use:

```java
var body = prometheus.scrape();
var contentType = PrometheusMetrics.CONTENT_TYPE;
```

`PrometheusMetrics.CONTENT_TYPE` is:

```text
text/plain; version=0.0.4; charset=utf-8
```

---

## Step 4 — Understand the metric mapping

| avaje metric | Prometheus output |
|---|---|
| `Counter` | `name_total` counter |
| `Timer` | `name_seconds` summary with `_count` and `_sum` samples |
| bucketed `Timer` | `name_seconds` histogram with `_bucket`, `_count`, and `_sum` samples |
| `Meter` | `name_count_total` and `name_total` counters |
| `GaugeLong` / `GaugeDouble` | `name` gauge |

Timer and meter `max` values are omitted by default because they are scrape-window values.
Enable them only when wanted:

```java
var prometheus = PrometheusMetrics.builder()
  .includeMax(true)
  .build();
```

---

## Step 5 — Verify

Record a few metrics:

```java
Metrics.counter("app.requests").inc();
Metrics.timer("app.service.run").time(service::run);
```

Then curl the endpoint:

```bash
curl -s http://localhost:8080/metrics
```

Confirm output similar to:

```text
# TYPE app_requests_total counter
app_requests_total 1
# TYPE app_service_run_seconds summary
app_service_run_seconds_count 1
app_service_run_seconds_sum 0.005
```

## Notes

- `PrometheusMetrics` is pull-based and does not start a scheduler.
- Prometheus counter output assumes avaje `Counter` values are used as increasing counters.
- Tags in `key:value` format are exported as Prometheus labels.
- Metric and label names are sanitized to Prometheus-compatible names.
- `timedThresholdMicros(...)` exists, but it applies to cumulative process-lifetime timer
  totals and is usually less useful for Prometheus scraping than for delta reporters.
- For OpenTelemetry-based Prometheus scraping, use `avaje-metrics-otel-producer` instead.
