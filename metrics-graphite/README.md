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

## Label tags

Graphite metric paths do not carry tags. When a metric has a `label:<value>` tag,
the reporter appends the label value to the Graphite metric path. For example,
`web.api` with `label:MyController.myMethod` is reported as
`web.api.MyController.myMethod.count`, `web.api.MyController.myMethod.total`,
and related timer series. The `app.component` base name is reported using the
legacy `app.<label>` path, such as `app.MyClass.myMethod.count`. Non-label tags
are ignored by this reporter.

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
