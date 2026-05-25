# avaje-metrics-graphite

Reports avaje-metrics data to Graphite and also provides a lower-level `GraphiteSender`
API for manual sending.

## Maven dependency

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

## Basic usage

```java
import io.avaje.metrics.graphite.GraphiteReporter;

GraphiteReporter reporter = GraphiteReporter.builder()
  .prefix("dev.my-app.")
  .hostname("graphite.example.com")
  .port(2003)
  .build();

reporter.report();
```

## Builder options

- `prefix(...)` — prepend a common metric path prefix such as `dev.my-app.`
- `hostname(...)` / `port(...)` — destination Graphite host and port
- `socketFactory(...)` — custom socket factory
- `batchSize(...)` — tune batching
- `timedThresholdMicros(...)` — suppress low-value timer metrics
- `registry(...)` — include a non-default registry or custom `MetricSupplier`
- `database(...)` — include Ebean database metrics directly
- `excludeDefaultRegistry()` — report only explicitly added registries/suppliers

## Manual sending

If you need low-level manual sending rather than reporter composition, use
`GraphiteSender` directly:

```java
GraphiteSender sender = GraphiteSender.builder()
  .hostname("graphite.example.com")
  .port(2003)
  .prefix("dev.my-app.")
  .build();

sender.connect();
sender.send("10", System.currentTimeMillis() / 1000, "test-metric", ".count");
sender.flush();
sender.close();
```

## More docs

- [Guide index](../docs/guides/README.md)
- [Getting started](../docs/guides/getting-started.md)
