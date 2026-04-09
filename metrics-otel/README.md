# avaje-metrics-otel

Exports [avaje-metrics](https://avaje-metrics.github.io) to [OpenTelemetry](https://opentelemetry.io/)
using the OTEL SDK Metrics API.

The module collects avaje metrics on a configurable schedule and pushes them to the provided
`OpenTelemetry` or `MeterProvider` instance. The OTEL SDK and its configured exporters
(OTLP, Prometheus, logging, etc.) handle shipping the data to your backend.

## Maven dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics-otel</artifactId>
  <version>9.8</version>
</dependency>
```

An `opentelemetry-api` dependency is required at runtime. This module declares it as `provided`,
so you must add the OTEL SDK and at least one exporter to your own project:

```xml
<!-- OTEL SDK + exporter of your choice -->
<dependency>
  <groupId>io.opentelemetry</groupId>
  <artifactId>opentelemetry-sdk</artifactId>
  <version>1.44.0</version>
</dependency>
<dependency>
  <groupId>io.opentelemetry</groupId>
  <artifactId>opentelemetry-exporter-otlp</artifactId>
  <version>1.44.0</version>
</dependency>
```

## Basic usage

```java
// Configure and build the reporter
OtelReporter reporter = OtelReporter.builder()
    .openTelemetry(openTelemetry)        // provide your configured OpenTelemetry instance
    .schedule(60, TimeUnit.SECONDS)      // report every 60 seconds (default)
    .build();

reporter.start();

// On shutdown:
reporter.close();
```

## Builder options

```java
OtelReporter reporter = OtelReporter.builder()

    // Provide the OpenTelemetry instance (preferred)
    .openTelemetry(openTelemetry)

    // Or provide a MeterProvider directly
    .meterProvider(meterProvider)

    // Use a non-default MetricRegistry (defaults to Metrics.registry())
    .registry(myRegistry)

    // Reporting interval (default: 60 seconds)
    .schedule(60, TimeUnit.SECONDS)

    // Skip timer metrics whose total duration is below this threshold.
    // Useful when @Timed is applied broadly and many methods are trivial.
    // 1_000 = skip timers with less than 1ms total execution per interval.
    .timedThresholdMicros(1_000)

    // OpenTelemetry instrumentation scope name (default: "io.avaje.metrics")
    .scopeName("io.avaje.metrics")

    .build();
```

## Metric mapping

avaje metrics are mapped to OTEL instruments as follows:

| avaje type | OTEL instruments | Notes |
|---|---|---|
| `Counter` | `LongCounter` | delta count per interval; OTEL SDK accumulates to cumulative |
| `Timer` | `LongCounter` (`name.count`), `LongCounter` (`name.total`), `LongGauge` (`name.max`) | values in microseconds (`us`) |
| `Meter` | `LongCounter` (`name.count`), `LongCounter` (`name.total`), `LongGauge` (`name.max`) | values in user-defined units |
| `GaugeLong` | `LongGauge` | current value, set each interval |
| `GaugeDouble` | `DoubleGauge` | current value, set each interval |

### Timer success and error

avaje timers track success and error events separately. They are reported as two distinct metric
name series: `name` (success) and `name.error` (errors). Each produces its own set of `count`,
`total`, and `max` instruments.

### Tags

avaje tags use a `key:value` colon-separated format. These are converted to OTEL `Attributes`:

```java
// avaje
Counter counter = registry.counter("app.login", Tags.of("env:prod", "region:us-east-1"));

// becomes OTEL attributes: {env="prod", region="us-east-1"}
```

## Reporting manually

If you prefer to manage the reporting schedule yourself rather than using the built-in scheduler:

```java
OtelReporter reporter = OtelReporter.builder()
    .openTelemetry(openTelemetry)
    .build();

// Call report() on your own schedule
reporter.report();
```

## Using with the OTEL Java agent

When running with the OpenTelemetry Java agent, the agent installs a global `OpenTelemetry`
instance. You can use it directly:

```java
import io.opentelemetry.api.GlobalOpenTelemetry;

OtelReporter reporter = OtelReporter.builder()
    .openTelemetry(GlobalOpenTelemetry.get())
    .schedule(60, TimeUnit.SECONDS)
    .build();

reporter.start();
```

## Example: avaje-inject

When using [avaje-inject](https://avaje.io/inject/), configure the reporter via a `@Factory` class.
`OtelReporter` implements `AutoCloseable`, so avaje-inject will call `close()` automatically
on container shutdown.

```java
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.metrics.otel.OtelReporter;
import io.opentelemetry.api.OpenTelemetry;
import java.util.concurrent.TimeUnit;

@Factory
class MetricsConfig {

  @Bean
  OpenTelemetry openTelemetry() {
    // setup via OTEL Java agent, autoconfiguration or manual SDK configuration
    // return GlobalOpenTelemetry.get();
    return AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk();
  }

  @Bean
  OtelReporter otelReporter(OpenTelemetry openTelemetry) {
    OtelReporter reporter = OtelReporter.builder()
        .openTelemetry(openTelemetry)
        .schedule(60, TimeUnit.SECONDS)
        .timedThresholdMicros(1_000)
        .build();
    reporter.start();
    return reporter;
    // close() is called automatically by avaje-inject on shutdown (AutoCloseable)
  }
}
```


## Example: Configure OTEL SDK with OTLP gRPC exporter

```java
// Configure OTEL SDK with OTLP gRPC exporter
SdkMeterProvider meterProvider = SdkMeterProvider.builder()
    .setResource(Resource.getDefault().toBuilder()
        .put(ServiceAttributes.SERVICE_NAME, "my-service")
        .build())
    .registerMetricReader(
        PeriodicMetricReader.builder(
            OtlpGrpcMetricExporter.builder()
                .setEndpoint("http://otel-collector:4317")
                .build())
        .setInterval(Duration.ofSeconds(60))
        .build())
    .build();

OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
    .setMeterProvider(meterProvider)
    .build();

OtelReporter reporter = OtelReporter.builder()
    .openTelemetry(openTelemetry)
    .timedThresholdMicros(1_000)
    .build();

reporter.start();
```

## Example: Prometheus scrape endpoint

```java
// Configure OTEL SDK with Prometheus exporter (pull-based)
PrometheusHttpServer prometheusServer = PrometheusHttpServer.builder()
    .setPort(9464)
    .build();

SdkMeterProvider meterProvider = SdkMeterProvider.builder()
    .registerMetricReader(prometheusServer)
    .build();

OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
    .setMeterProvider(meterProvider)
    .build();

OtelReporter reporter = OtelReporter.builder()
    .openTelemetry(openTelemetry)
    .build();

reporter.start();
```

Metrics are then available at `http://localhost:9464/metrics` for Prometheus to scrape.
