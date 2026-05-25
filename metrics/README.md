# avaje-metrics

Core metrics library providing timers, counters, meters, gauges, built-in JVM metrics,
and the default `MetricRegistry`.

## Maven dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics</artifactId>
  <version>${version}</version>
</dependency>
```

If the application uses `module-info.java`, also add:

```java
requires io.avaje.metrics;
```

## Basic usage

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

requests.inc();
timer.time(service::run);
bytesSent.addEvent(4_096);
```

## Default vs custom registry

Most applications use the default registry via `Metrics`:

```java
var registry = Metrics.registry();
```

Create a separate registry only when you want isolation:

```java
var registry = Metrics.createRegistry();
var timer = registry.timerBuilder("app.db.query").build();
```

## Built-in JVM metrics

```java
Metrics.jvmMetrics()
  .registerJvmCoreMetrics();
```

Use `registerJvmMetrics()` for the fuller built-in set.

## Method timing

Programmatic timing:

```java
var timer = Metrics.timer("app.service.run");
timer.time(service::run);
```

Traced timers:

```java
var timer = Metrics.timerBuilder("app.service.run")
  .buildTraced();
```

`@Timed` is the declarative path when enhancement is already enabled in the application.

## Next steps

- [Guide index](../docs/guides/README.md)
- [Getting started](../docs/guides/getting-started.md)
- [Register JVM metrics](../docs/guides/register-jvm-metrics.md)
- [Add method timing](../docs/guides/add-method-timing.md)
- [Add OpenTelemetry export](../docs/guides/add-open-telemetry-export.md)
