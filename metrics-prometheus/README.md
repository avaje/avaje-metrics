# avaje-metrics-prometheus

Exports [avaje-metrics](https://avaje-metrics.github.io) in Prometheus text exposition
format.

This module is a lightweight pull/scrape exporter. It does not run a scheduler and does
not start an HTTP server; expose the scrape body from the web framework already used by
the application.

## Maven dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics-prometheus</artifactId>
  <version>${version}</version>
</dependency>
```

If the application uses `module-info.java`, also add:

```java
requires io.avaje.metrics.prometheus;
```

## Basic usage

```java
import io.avaje.metrics.Metrics;
import io.avaje.metrics.prometheus.PrometheusMetrics;

var prometheus = PrometheusMetrics.builder()
  .registry(Metrics.registry())
  .build();

String body = prometheus.scrape();
```

For an HTTP endpoint, return:

- body: `prometheus.scrape()`
- content type: `PrometheusMetrics.CONTENT_TYPE`

You can also stream to an `Appendable`:

```java
prometheus.write(writer);
```

## Builder options

```java
var prometheus = PrometheusMetrics.builder()

  // Use a non-default MetricRegistry (defaults to Metrics.registry())
  .registry(myRegistry)

  // Advanced: skip timer metrics whose cumulative total duration is below this threshold.
  // This is usually less useful for Prometheus because scrapes use cumulative values.
  .timedThresholdMicros(1_000)

  // Include scrape-window max gauges for timers and meters (disabled by default).
  .includeMax(true)

  .build();
```

## Metric mapping

avaje metrics are collected using `CollectionMode.CUMULATIVE` and mapped as follows:

| avaje type | Prometheus output |
|---|---|
| `Counter` | `name_total` counter |
| `Timer` | `name_seconds` summary with `_count` and `_sum` samples |
| bucketed `Timer` | `name_seconds` histogram with `_bucket`, `_count`, and `_sum` samples |
| `Meter` | `name_count_total` and `name_total` counters |
| `GaugeLong` / `GaugeDouble` | `name` gauge |

`max` values are disabled by default because avaje cumulative collection still treats max
as a scrape-window value. Enable `.includeMax(true)` only when that behavior is useful.

`timedThresholdMicros(...)` is available as an advanced option, but it applies to the
cumulative process-lifetime timer total, so it is usually less useful for Prometheus
scraping than it is for delta/scheduled reporters.

## More docs

- [Guide index](../docs/guides/README.md)
- [Add Prometheus scraping](../docs/guides/add-prometheus-scrape.md)
- [Getting started](../docs/guides/getting-started.md)
