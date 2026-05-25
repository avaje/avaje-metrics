# Guide: Add StatsD Reporting

## Purpose

This guide provides step-by-step instructions for exporting **avaje-metrics** data to
StatsD / DogStatsD using `avaje-metrics-statsd`.

When asked to *"add StatsD reporting"*, *"send metrics to DogStatsD"*, or *"configure
`StatsdReporter`"* in a project, follow these steps exactly.

---

## Overview

`avaje-metrics-statsd` reports avaje-metrics data on a schedule using the Datadog Java
StatsD client.

The usual pattern is:

1. add `avaje-metrics-statsd`
2. build a `StatsdReporter`
3. start it during application startup
4. close it on shutdown

---

## Step 1 — Add the dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics-statsd</artifactId>
  <version>${version}</version>
</dependency>
```

---

## Step 2 — Build and start the reporter

```java
import io.avaje.metrics.statsd.StatsdReporter;

import java.util.concurrent.TimeUnit;

StatsdReporter reporter = StatsdReporter.builder()
  .hostname("localhost")
  .port(8125)
  .tags(new String[]{"env:dev", "service:billing"})
  .schedule(60, TimeUnit.SECONDS)
  .timedThresholdMicros(1_000)
  .build()
  .start();
```

Close the reporter on shutdown:

```java
reporter.close();
```

---

## Step 3 — Add custom registries or database metrics when needed

Use a non-default registry:

```java
var registry = Metrics.createRegistry();

StatsdReporter reporter = StatsdReporter.builder()
  .registry(registry)
  .build()
  .start();
```

Include Ebean database metrics directly:

```java
StatsdReporter reporter = StatsdReporter.builder()
  .database(database)
  .build()
  .start();
```

Use `databaseVerbose(database)` when you explicitly want the more detailed database path.

---

## Step 4 — Use a custom client only when necessary

If the project already creates its own `StatsDClient`, you can supply it directly:

```java
StatsdReporter reporter = StatsdReporter.builder()
  .client(customStatsdClient)
  .build()
  .start();
```

When `client(...)` is used, the hostname, port, and tags configuration on the builder is
not used.

---

## Step 5 — Verify

1. Start the application with the reporter running.
2. Record a few metrics:

```java
Metrics.counter("app.requests").inc();
Metrics.timer("app.service.run").time(service::run);
```

3. Confirm the expected metric names and tags arrive in StatsD / DogStatsD.

## Notes

- `schedule(...)` controls how often avaje-metrics is collected and sent.
- `timedThresholdMicros(...)` is useful for suppressing low-value timers when timing is
  applied broadly.
- `registry(...)`, `database(...)`, and `reporter(...)` can be combined when multiple
  metric sources need to be exported.
