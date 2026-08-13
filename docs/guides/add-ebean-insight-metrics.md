# Guide: Add Ebean Insight Metrics

## Purpose

Use `EbeanInsightProvider` when the application should report Avaje metrics
and one or more Ebean database snapshots to an Ebean Insight server, while also
returning a filtered Avaje view for OTEL or StatsD.

## Dependencies

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics-ebean-insight</artifactId>
  <version>${version}</version>
</dependency>
```

The provider uses `ebean-insight` 2.2 or later, which supports sending multiple
database snapshots with an externally collected Avaje metrics list.

## Create the provider

```java
import io.avaje.metrics.ebean.insight.EbeanInsightProvider;
import io.ebean.insight.InsightClient;

var insight = InsightClient.builder()
    .url("https://insight.example.com")
    .key("secret")
    .appName("orders")
    .environment("prod")
    .capturePlansFor(primaryDatabase)
    .collectEbeanMetrics(false)
    .build();

var provider = EbeanInsightProvider.builder(insight, primaryDatabase)
    .database(auditDatabase)
    .build();
```

The provider owns unregistered `DatabaseMetricSupplier` instances for its
databases. Do not also register those databases with the same registry using
`DatabaseMetricSupplier` or `InsightClient.register(...)`, because that would
poll their reset-on-read metrics more than once.

## Use with OTEL or StatsD

```java
OtelReporter.builder()
    .openTelemetry(openTelemetry)
    .metricsProvider(provider)
    .build()
    .start();
```

or:

```java
StatsdReporter.builder()
    .metricsProvider(provider)
    .build()
    .start();
```

On each reporting interval the provider:

1. Collects the registry metrics.
2. Collects each configured database once.
3. Sends the raw database snapshots and the filtered Insight metric projection
   to Insight.
4. Returns application metrics, converted Ebean metrics, and datasource pool
   metrics for OTEL or StatsD.

## Filtering

Use separate predicates for the two destinations:

```java
var provider = EbeanInsightProvider.builder(insight, primaryDatabase)
    .database(auditDatabase)
    .insightFilter(statistics -> !statistics.name().startsWith("debug."))
    .exportFilter(statistics -> !statistics.name().startsWith("internal."))
    .build();
```

The Insight projection includes datasource pool metrics but does not add the
converted Ebean query/timer/counter statistics, because those are already
represented by the raw Ebean `ServerMetrics` database payload. The OTEL/StatsD
projection includes the converted Ebean statistics.
