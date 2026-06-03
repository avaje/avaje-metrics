# Guide: Configure OpenTelemetry environment variables

## Purpose

This guide is the **reference for environment variables and system properties** read by
`avaje-metrics-otel` when configuring exporters, intervals, timeouts, and resource
attributes.

When asked to *"configure OTel via env vars"*, *"set the OTLP endpoint without code"*,
*"deploy avaje-metrics OTel with environment-specific settings"*, or *"use standard
OpenTelemetry environment variables"*, follow this guide.

For end-to-end setup of the SDK itself, see:

- [Add OpenTelemetry export](add-open-telemetry-export.md)
- [Add OpenTelemetry export — AWS Lambda](add-open-telemetry-lambda.md)

---

## When to use environment variables vs builder calls

Prefer environment variables (or system properties) when a value:

- changes per deployment environment (TEST / PROD endpoint, deployment.environment.name)
- is operationally tunable (export intervals, timeouts) and you do not want a code change
  to adjust it
- follows the OpenTelemetry specification convention (`OTEL_*`) so it can be set the same
  way for any other OTel-enabled component in the same process

Prefer explicit builder calls when a value:

- is part of the application's identity and should not vary (e.g. `service.namespace`,
  business domain attributes)
- is not exposed by the OTel SDK env var convention

In all cases, **explicit builder calls override environment variables**.

---

## Precedence

For each value:

1. Explicit `Builder` method (e.g. `.endpoint(...)`, `.meterInterval(...)`)
2. System property (e.g. `-Dotel.exporter.otlp.endpoint=...`)
3. Environment variable (e.g. `OTEL_EXPORTER_OTLP_ENDPOINT=...`)
4. Built-in default

The first non-blank value wins. Values found in the OTel SDK conventional env vars are
read once at SDK build time.

---

## Reference

### Resource attributes

| Variable | System property | Purpose |
|---|---|---|
| `OTEL_SERVICE_NAME` | `otel.service.name` | Sets `service.name` resource attribute. Overrides `service.name` set via `OTEL_RESOURCE_ATTRIBUTES`. |
| `OTEL_RESOURCE_ATTRIBUTES` | `otel.resource.attributes` | Comma-separated `key=value` resource attributes (URL-decoded). Merged with `Resource.getDefault()`. |
| `OTEL_DEPLOYMENT_ENVIRONMENT_NAME` | `otel.deployment.environment.name` | Sets the `deployment.environment.name` resource attribute as a dedicated convenience env var. Overrides any value supplied via `OTEL_RESOURCE_ATTRIBUTES`. Overridden by an explicit `.deploymentEnvironmentName(...)` or `.resourceAttribute("deployment.environment.name", ...)` builder call. |

`OTEL_DEPLOYMENT_ENVIRONMENT_NAME` is useful in Infrastructure-as-Code (CloudFormation,
Terraform) setups where most resource attributes are static across environments but
`deployment.environment.name` is computed per stack.

### OTLP exporter

| Variable | System property | Purpose |
|---|---|---|
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `otel.exporter.otlp.endpoint` | OTLP collector endpoint. Used when `Builder.endpoint(...)` is not set. |
| `OTEL_EXPORTER_OTLP_PROTOCOL` | `otel.exporter.otlp.protocol` | OTLP wire protocol. Recognised values: `grpc`, `http/protobuf`. Used when `Builder.protocol(...)` is not set. Default is `grpc`. The value `http/json` is rejected. |
| `OTEL_EXPORTER_OTLP_TIMEOUT` | `otel.exporter.otlp.timeout` | Per-export timeout in **milliseconds** (per the OTel spec). Applies to both connect and request timeouts when `Builder.connectTimeout(...)` / `Builder.exportTimeout(...)` are not set. |

### Intervals

| Variable | System property | Purpose |
|---|---|---|
| `OTEL_METRIC_EXPORT_INTERVAL` | `otel.metric.export.interval` | Periodic metric reader interval in **milliseconds**. Used when `Builder.meterInterval(...)` is not set. |
| `OTEL_BSP_SCHEDULE_DELAY` | `otel.bsp.schedule.delay` | Batch span processor schedule delay in **milliseconds**. Used when `Builder.traceInterval(...)` is not set. |

### Trace sampling

| Variable | System property | Purpose |
|---|---|---|
| `OTEL_TRACES_SAMPLER` | `otel.traces.sampler` | One of `parentbased_traceidratio`, `always_on`, `always_off`. |
| `OTEL_TRACES_SAMPLER_ARG` | `otel.traces.sampler.arg` | Numeric ratio (0.0–1.0) for `parentbased_traceidratio`. |

See [Add OpenTelemetry export](add-open-telemetry-export.md) for sampler details.

---

## Worked example: AWS Lambda CloudFormation

Code stays minimal:

```java
var result = MetricsOpenTelemetry.builder()
  .resourceAttribute("service.namespace", "consolidation")
  .enableWaitIfRunning()
  .timeout(Duration.ofSeconds(35))
  .buildAndRegisterGlobal();
```

Configuration is supplied per environment via Lambda env vars:

```yaml
Environment:
  Variables:
    OTEL_SERVICE_NAME: my-lambda
    OTEL_EXPORTER_OTLP_ENDPOINT: !Ref OtelEndpoint
    OTEL_EXPORTER_OTLP_PROTOCOL: http/protobuf
    OTEL_DEPLOYMENT_ENVIRONMENT_NAME: !Ref Environment
    OTEL_RESOURCE_ATTRIBUTES: !Ref OtelResourceAttributes
    OTEL_TRACES_SAMPLER: parentbased_traceidratio
    OTEL_TRACES_SAMPLER_ARG: !Ref OtelTraceSampleRatio
```

Where `OtelResourceAttributes` is a static, environment-agnostic set such as
`business.domain=ingestion,business.platform=aws,business.system=consolidation`.

---

## Verification

To confirm a setting was picked up, log the resolved attributes once at startup or
inspect the `Resource` on a captured metric/span. For example:

```java
var sdk = MetricsOpenTelemetry.builder().buildAndRegisterGlobal();
// Force a flush and inspect collector logs / Tempo / Mimir for resource attributes.
```

Common pitfalls:

- `OTEL_*_INTERVAL` / `OTEL_*_DELAY` / `OTEL_EXPORTER_OTLP_TIMEOUT` are in **milliseconds**.
  Setting `60` means 60ms, not 60s.
- The OTel gateway may silently drop signals missing required resource attributes. Verify
  the full set of `service.namespace`, `business.*`, and `deployment.environment.name` is
  present on the `Resource` of an emitted metric.
- An explicit `Builder.endpoint(...)` call overrides `OTEL_EXPORTER_OTLP_ENDPOINT`. Remove
  the builder call if you want the env var to take effect.

---

## See also

- [Add OpenTelemetry export](add-open-telemetry-export.md)
- [Add OpenTelemetry export — AWS Lambda](add-open-telemetry-lambda.md)
- [OpenTelemetry SDK environment variable specification](https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/)
