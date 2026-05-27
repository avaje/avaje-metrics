# Guide: Getting Started

## Purpose

This guide provides step-by-step instructions for adding **avaje-metrics** to a project,
creating the first metrics, and understanding the default registry.

When asked to *"add avaje-metrics"*, *"get started with metrics"*, *"create counters or
timers"*, or *"set up the default registry"* in a Java project, follow these steps
exactly.

---

## Overview

avaje-metrics is centered around a `MetricRegistry` and four main metric types:

| Type | Purpose | Example |
|---|---|---|
| `Counter` | Count events | requests, retries, errors |
| `Timer` | Measure execution time | service calls, HTTP handlers |
| `Meter` | Record value-carrying events | bytes sent, rows processed |
| `Gauge` | Read a current value from a supplier | queue depth, memory usage |

Most applications use the default registry via the static `Metrics` helper and enable
build-time enhancement for `@Timed`, then choose an export path such as OpenTelemetry,
Prometheus, StatsD, or Graphite separately.

---

## Step 1 — Add the dependency

Add `avaje-metrics` to `pom.xml`:

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics</artifactId>
  <version>${version}</version>
</dependency>
```

If the project uses `module-info.java`, also add:

```java
requires io.avaje.metrics;
```

---

## Step 2 — Add build-time enhancement

Most avaje-metrics applications use build-time enhancement for declarative method timing
with `@Timed`. Add the metrics Maven plugin under `pom.xml` / `build` / `plugins`:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>io.avaje.metrics</groupId>
      <artifactId>metrics-maven-plugin</artifactId>
      <version>${avaje-metrics.version}</version>
      <extensions>true</extensions>
    </plugin>
  </plugins>
</build>
```

This enhances compiled classes during the Maven build. No runtime `-javaagent` setup is
needed for the common Maven path.

---

## Step 3 — Start with the default registry

The simplest path is to use the default registry through `Metrics`.

```java
import io.avaje.metrics.Metrics;
import io.avaje.metrics.MetricRegistry;

MetricRegistry registry = Metrics.registry();
```

Most applications do not need to create a separate registry unless they want to isolate
metric sets or manage collection independently.

If a separate registry is needed:

```java
MetricRegistry registry = Metrics.createRegistry();
```

---

## Step 4 — Create the first metrics

```java
import io.avaje.metrics.Metrics;
import io.avaje.metrics.Tags;

var requests = Metrics.counterBuilder("app.http.requests")
  .unit("{event}")
  .build();

var timer = Metrics.timerBuilder("app.service.run")
  .tags(Tags.of("operation:sync"))
  .build();

var bytesSent = Metrics.meterBuilder("app.bytes.sent")
  .unit("By")
  .build();

Metrics.gauge("app.queue.depth")
  .ofLongs(queue::size);
```

Metric naming guidance:

- use stable, dotted names such as `app.http.requests`
- use tags for dimensions like `env:prod` or `operation:sync`
- use units where they add real meaning, such as `By`, `row`, or `MiBy`

---

## Step 5 — Record values

```java
requests.inc();
requests.inc(42);

timer.time(service::run);

bytesSent.addEvent(4_096);
```

If you want explicit timer lifecycle control:

```java
var event = timer.startEvent();
try {
  service.run();
  event.end();
} catch (RuntimeException e) {
  event.endWithError(e);
  throw e;
}
```

---

## Step 6 — Inspect collected metrics

For a quick sanity check, collect metrics from the default registry:

```java
var metrics = Metrics.collectMetrics();
var json = Metrics.collectAsJson().asJson();
```

This is useful for verification during local setup even if the application will later use
an exporter module.

---

## Next steps

- For built-in JVM metrics, see [register-jvm-metrics.md](register-jvm-metrics.md)
- For method timing and traced timers, see [add-method-timing.md](add-method-timing.md)
- For build-time enhancement options, see [configure-metrics-agent.md](configure-metrics-agent.md)
- For OpenTelemetry export, see [add-open-telemetry-export.md](add-open-telemetry-export.md)
- For Prometheus scraping, see [add-prometheus-scrape.md](add-prometheus-scrape.md)

## Notes

- `Metrics.registry()` returns the default shared registry.
- `Metrics.createRegistry()` creates a separate registry when isolation is needed.
- `metrics-maven-plugin` enables the common build-time enhancement path for `@Timed`.
- Export is a separate concern; `avaje-metrics` collects metrics, while modules such as
  OpenTelemetry, Prometheus, StatsD, or Graphite handle shipping them elsewhere.
