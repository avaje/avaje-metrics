# avaje-metrics

Java metrics library for timers, counters, meters, gauges, built-in JVM metrics, and
export paths such as OpenTelemetry, Prometheus, StatsD, and Graphite.

The repository contains the core `avaje-metrics` module plus optional integration and
export modules.

## Modules

| Artifact | Purpose | Docs |
|---|---|---|
| `avaje-metrics` | Core metrics API, default registry, JVM metrics, `@Timed`, traced timer support hooks | [metrics/README.md](metrics/README.md) |
| `avaje-metrics-otel` | Convenience OTLP-backed OpenTelemetry setup for metrics + traces | [metrics-otel/README.md](metrics-otel/README.md) |
| `avaje-metrics-otel-producer` | OpenTelemetry SDK `MetricProducer` bridge | [metrics-otel-producer/README.md](metrics-otel-producer/README.md) |
| `avaje-metrics-otel-trace` | OpenTelemetry span bridge for traced timers | [metrics-otel-trace/README.md](metrics-otel-trace/README.md) |
| `avaje-metrics-otel-reporter` | Scheduled OpenTelemetry reporter path | [metrics-otel-reporter/README.md](metrics-otel-reporter/README.md) |
| `avaje-metrics-prometheus` | Prometheus text exposition scrape exporter | [metrics-prometheus/README.md](metrics-prometheus/README.md) |
| `avaje-metrics-statsd` | StatsD / DogStatsD reporter | [metrics-statsd/README.md](metrics-statsd/README.md) |
| `avaje-metrics-graphite` | Graphite reporter and sender | [metrics-graphite/README.md](metrics-graphite/README.md) |
| `avaje-metrics-ebean` | Ebean `MetricSupplier` integration | [metrics-ebean/README.md](metrics-ebean/README.md) |

## Documentation & Guides

- [Main website](https://avaje-metrics.github.io)
- [Docs landing page](docs/README.md)
- [Guide index](docs/guides/README.md)
- [Getting started](docs/guides/getting-started.md)
- [Register JVM metrics](docs/guides/register-jvm-metrics.md)
- [Add method timing](docs/guides/add-method-timing.md)
- [Add OpenTelemetry export](docs/guides/add-open-telemetry-export.md)
- [Add Prometheus scraping](docs/guides/add-prometheus-scrape.md)

## Quick start

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics</artifactId>
  <version>${version}</version>
</dependency>
```

```java
import io.avaje.metrics.Metrics;
import io.avaje.metrics.Tags;

var requests = Metrics.counterBuilder("app.http.requests")
  .unit("{event}")
  .build();

var timer = Metrics.timerBuilder("app.service.run")
  .tags(Tags.of("operation:sync"))
  .build();

Metrics.gauge("app.queue.depth")
  .ofLongs(queue::size);

Metrics.jvmMetrics()
  .withReportAlways()
  .registerJvmCoreMetrics();

requests.inc();
timer.time(service::run);
```

From there, choose an export path:

- OpenTelemetry: [docs/guides/add-open-telemetry-export.md](docs/guides/add-open-telemetry-export.md)
- Prometheus: [docs/guides/add-prometheus-scrape.md](docs/guides/add-prometheus-scrape.md)
- StatsD: [metrics-statsd/README.md](metrics-statsd/README.md)
- Graphite: [metrics-graphite/README.md](metrics-graphite/README.md)

## License

Published under Apache License 2.0, see [LICENSE](LICENSE).
