# Guides

**Recommended:** For all downstream projects, add an `AGENTS.md` at the project root.
`AGENTS.md` should point both AI and human developers to the official agent/developer
guides for all major frameworks used. This ensures fast, accurate onboarding and
discoverability of best practices.

See: [how-to-add-AGENTS-md.md](how-to-add-AGENTS-md.md) for a step-by-step guide and
template.

See also: [AGENTS.md](AGENTS.md) — a minimal template for AI agent onboarding and
automation in avaje-metrics projects.

Step-by-step guides written as instructions for AI agents and developers working in
**avaje-metrics** projects.

## Project context

**avaje-metrics** projects are Java applications or libraries built around:

- timers, counters, gauges, and meters
- a default or custom `MetricRegistry`
- optional JVM metrics registration
- optional exporters/integrations such as OpenTelemetry, Prometheus, StatsD, Graphite, and Ebean
- Maven as the build tool
- Java 11+

---

## Getting Started

| Guide | Description |
|-------|-------------|
| [Getting started](getting-started.md) | Add `avaje-metrics`, create your first metrics, understand the default registry, and choose the next export/integration path |

## Core metrics

| Guide | Description |
|-------|-------------|
| [Register JVM metrics](register-jvm-metrics.md) | Register the built-in JVM metric sets, choose between core and detailed metrics, and apply global tags |
| [Add method timing](add-method-timing.md) | Add programmatic timers, traced timers, and `@Timed`-based method timing for service methods and components |
| [Configure metrics enhancement](configure-metrics-agent.md) | Enable build-time `@Timed` enhancement with `metrics-maven-plugin`, configure `metrics.mf`, and control naming, spans, and component detection |

## Exporters / observability

| Guide | Description |
|-------|-------------|
| [Add OpenTelemetry export](add-open-telemetry-export.md) | Choose between `avaje-metrics-otel`, `avaje-metrics-otel-producer`, `avaje-metrics-otel-trace`, and `avaje-metrics-otel-reporter` and wire the right OTEL path |
| [Add Prometheus scraping](add-prometheus-scrape.md) | Add `avaje-metrics-prometheus` and expose a pull-based Prometheus text endpoint using cumulative collection |
| [Add StatsD reporting](add-statsd-reporting.md) | Add `avaje-metrics-statsd`, configure `StatsdReporter`, and export avaje-metrics data to StatsD / DogStatsD |
| [Add Graphite reporting](add-graphite-reporting.md) | Add `avaje-metrics-graphite`, configure `GraphiteReporter`, and export avaje-metrics data to Graphite |

## Integrations

| Guide | Description |
|-------|-------------|
| [Add Ebean metrics](add-ebean-metrics.md) | Add `avaje-metrics-ebean`, register `DatabaseMetricSupplier`, and expose Ebean database metrics through avaje-metrics |

---

## Helping AI agents find these guides

AI coding agents can only follow these guides if they know they exist. The recommended
approach is to add an `AGENTS.md` at the project root, pointing both AI and human
developers to the official agent/developer guides for all major frameworks used.

See: [how-to-add-AGENTS-md.md](how-to-add-AGENTS-md.md) for a step-by-step guide and
template.

Below are copy-paste snippets for the most common AI tooling configurations.

### Project `AGENTS.md` (recommended)

```markdown
# AI Agent Instructions

This project uses [avaje-metrics](https://avaje-metrics.github.io). Step-by-step guides
for common tasks (getting started, JVM metrics, method timing, metrics enhancement configuration,
OpenTelemetry export, Prometheus scraping, StatsD reporting, Graphite reporting, Ebean metrics)
are at:

**https://github.com/avaje/avaje-metrics/tree/HEAD/docs/guides/**
```

### Project `README.md` (legacy/universal)

```markdown
## AI Agent Instructions

This project uses [avaje-metrics](https://avaje-metrics.github.io). Step-by-step guides
for common tasks (getting started, JVM metrics, method timing, metrics enhancement configuration,
OpenTelemetry export, Prometheus scraping, StatsD reporting, Graphite reporting, Ebean metrics)
are at:

**https://github.com/avaje/avaje-metrics/tree/HEAD/docs/guides/**
```

### GitHub Copilot — `.github/copilot-instructions.md`

`docs/guides/README.md` (this file) is the single source of truth for AI agent
instructions in this repository. For **your project** that uses avaje-metrics as a
dependency, add the following to your `.github/copilot-instructions.md`:

```markdown
## avaje-metrics

This project uses [avaje-metrics](https://avaje-metrics.github.io). Step-by-step guides
for common tasks are at: https://github.com/avaje/avaje-metrics/tree/HEAD/docs/guides/

Key guides (fetch and follow when performing the relevant task):
- Getting started: https://raw.githubusercontent.com/avaje/avaje-metrics/HEAD/docs/guides/getting-started.md
- Register JVM metrics: https://raw.githubusercontent.com/avaje/avaje-metrics/HEAD/docs/guides/register-jvm-metrics.md
- Add method timing: https://raw.githubusercontent.com/avaje/avaje-metrics/HEAD/docs/guides/add-method-timing.md
- Configure metrics enhancement: https://raw.githubusercontent.com/avaje/avaje-metrics/HEAD/docs/guides/configure-metrics-agent.md
- Add OpenTelemetry export: https://raw.githubusercontent.com/avaje/avaje-metrics/HEAD/docs/guides/add-open-telemetry-export.md
- Add Prometheus scraping: https://raw.githubusercontent.com/avaje/avaje-metrics/HEAD/docs/guides/add-prometheus-scrape.md
- Add StatsD reporting: https://raw.githubusercontent.com/avaje/avaje-metrics/HEAD/docs/guides/add-statsd-reporting.md
- Add Graphite reporting: https://raw.githubusercontent.com/avaje/avaje-metrics/HEAD/docs/guides/add-graphite-reporting.md
- Add Ebean metrics: https://raw.githubusercontent.com/avaje/avaje-metrics/HEAD/docs/guides/add-ebean-metrics.md
```

### Claude Code — `CLAUDE.md`

Same content as above — Claude Code reads `CLAUDE.md` at the project root.

### Cursor — `.cursor/rules/avaje-metrics.mdc`

```markdown
---
description: avaje-metrics task guidance
globs: ["**/*.java", "**/pom.xml"]
alwaysApply: false
---

## avaje-metrics

This project uses avaje-metrics. Before performing any avaje-metrics-related task,
fetch and follow the relevant guide from:
https://github.com/avaje/avaje-metrics/tree/HEAD/docs/guides/
```
