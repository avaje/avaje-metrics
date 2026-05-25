# avaje-metrics-statsd

Exports avaje-metrics to StatsD / DogStatsD using the Datadog Java client.

## Maven dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics-statsd</artifactId>
  <version>${version}</version>
</dependency>
```

## Basic usage

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

// close on shutdown
reporter.close();
```

## Common options

- `hostname(...)` / `port(...)` — destination host and port
- `tags(...)` — common StatsD tags
- `client(...)` — supply a custom `StatsDClient`
- `schedule(...)` — reporting interval
- `timedThresholdMicros(...)` — suppress low-value timer metrics
- `registry(...)` — use a non-default registry
- `database(...)` / `databaseVerbose(...)` — include Ebean metrics directly

If `client(...)` is used, the hostname, port, and tags settings are ignored.

## More docs

- [Guide index](../docs/guides/README.md)
- [Getting started](../docs/guides/getting-started.md)
- [Add StatsD reporting](../docs/guides/add-statsd-reporting.md)
