# Guide: Configure Metrics Enhancement

## Purpose

This guide provides step-by-step instructions for enabling **avaje-metrics**
method timing enhancement with the `metrics-maven-plugin` and configuring it
with `metrics.mf`.

Use this guide when asked to *"enable @Timed"*, *"configure metrics-agent"*,
*"add metrics.mf"*, or *"turn on method timing enhancement"* in a Maven
project.

---

## Overview

The usual way to enable avaje-metrics enhancement is build-time enhancement with
the Maven plugin:

```xml
<plugin>
  <groupId>io.avaje.metrics</groupId>
  <artifactId>metrics-maven-plugin</artifactId>
  <version>${avaje-metrics.version}</version>
  <extensions>true</extensions>
</plugin>
```

With `<extensions>true</extensions>`, Maven binds the plugin's `enhance` goal to
`process-classes`. The plugin enhances compiled application classes in
`target/classes` during the build. This is the path used by the vast majority of
avaje-metrics users and avoids any runtime `-javaagent` command-line setup.

The Maven plugin uses `metrics-agent` internally for the bytecode transform. The
user-facing configuration still lives in `metrics.mf`.

Use enhancement when you want declarative timing with:

- `@Timed` on classes or methods
- automatic timing for Avaje HTTP controllers
- optional automatic timing for `@Singleton`, Spring, JAX-RS, or JEE components

If you do not want bytecode enhancement, use programmatic timers instead:

```java
var timer = Metrics.timer("app.service.run");
timer.time(service::run);
```

---

## Step 1 - Add the application dependency

The application needs the normal metrics dependency:

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics</artifactId>
  <version>${avaje-metrics.version}</version>
</dependency>
```

---

## Step 2 - Add build-time enhancement

Add the metrics Maven plugin under `pom.xml` / `build` / `plugins`:

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

No `metrics-agent` application dependency is required for the common Maven setup.
The plugin brings in the agent and applies enhancement at build time.

---

## Step 3 - Optionally add `metrics.mf`

Most applications do not need a `metrics.mf` file. Start with the defaults and only add
`metrics.mf` when you want to override enhancement behavior.

The defaults cover the common cases:

- explicit `@Timed` on classes or methods
- Avaje HTTP controllers
- `@Singleton` and Avaje Inject components
- spans default off
- full metric-name based timing

When you need to override defaults, create:

```text
src/main/resources/metrics.mf
```

Use Java manifest-style `key: value` lines. Prefer no comments, avoid blank
lines in the middle of the file, and keep one option per line.

Example overrides:

```text
timedMetricNaming: label-tag
nameTrimPackages: com.example
```

Tracing default override:

```text
timedSpans: default-child
```

You generally do not need to set `packages`. The built-in filtering skips JDK and
common library packages, and with build-time enhancement the priority is simple setup
rather than minimizing runtime class-loading overhead. Use `packages` only when you
want an explicit allow-list for a large or unusual build.

The enhancer reads configuration resources from the build classpath:

1. all `metrics-common.mf` resources
2. all `metrics.mf` resources

Use `metrics-common.mf` only for shared defaults across modules. Put the final
application enhancement policy in `metrics.mf`.

---

## Step 4 - Choose what is enhanced

Explicit timing always works:

```java
import io.avaje.metrics.annotation.NotTimed;
import io.avaje.metrics.annotation.Timed;

@Timed(tags = "component:billing")
class BillingService {

  void syncInvoices() {
    // timed
  }

  @NotTimed
  void helper() {
    // not timed
  }
}
```

By default, class-level timing applies to public methods. Constructors,
`equals`, `hashCode`, `toString`, `init`, and `postConfigured` are excluded.

The enhancer also detects Avaje HTTP controllers:

```java
import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;

@Controller("/customers")
class CustomerController {

  @Get("/{id}")
  Customer find(long id) {
    return service.find(id);
  }
}
```

Avaje HTTP controllers use `web.api` as the default metric base.

---

## Step 5 - Configure enhancement options

| Key | Default | Description |
|---|---:|---|
| `packages` | none | Optional package allow-list. Usually omit this and use the built-in filtering. Values are split on comma, semicolon, or space. Supports `*` and `**` suffixes. |
| `debugLevel` | `0` | Transform logging verbosity. Use `1` for basic diagnostics and higher values for more detail. |
| `timedSpans` | `default-off` | Span defaults: `default-off`, `default-child`, or `disabled`. |
| `timedMetricNaming` | `full-name` | `full-name` or `label-tag`. |
| `includeStaticMethods` | `false` | Include static methods when a class is enhanced by default. |
| `enhanceSingleton` | `true` | Enhance classes annotated with `@Singleton`. |
| `enhanceAvajeComponent` | `true` | Enhance Avaje Inject component classes. |
| `enhanceNonPrivate` | `false` | Include package-private and protected methods for class-level timing. |
| `readOnly` | `false` | Run detection/logging without writing transformed bytecode. Useful for debugging. |
| `spring` | `false` | Auto-detect Spring stereotype components. |
| `jaxrs` | `false` | Auto-detect JAX-RS endpoint annotations. |
| `jee` | `false` | Auto-detect JEE EJB/WebService annotations. |
| `nameIncludePackages` | `false` | Include package names in generated metric names or labels. |
| `nameTrimPackages` | none | Package prefixes to trim from names. Values are split on comma, semicolon, or space. |

Example for Spring and JAX-RS applications:

```text
spring: true
jaxrs: true
enhanceNonPrivate: true
```

Example for build-time inspection only:

```text
debugLevel: 4
readOnly: true
```

Run a build and inspect the plugin output:

```bash
mvn process-test-classes
```

---

## Step 6 - Control naming, tags, and buckets

Use `@Timed` for local naming and dimensions:

```java
@Timed(prefix = "app.billing", tags = {"component:billing", "marker:blue"})
class BillingService {

  @Timed(name = "sync", tags = "operation:sync", buckets = {100, 200, 500})
  void syncInvoices() {
  }
}
```

With the default naming mode, this creates a timer named:

```text
app.billing.BillingService.sync
```

With:

```text
timedMetricNaming: label-tag
```

the generated timer uses the class or controller base name and adds a
`label:<class.method>` tag. This can reduce metric-name cardinality while keeping
method identity as a tag.

---

## Step 7 - Configure traced timers

Enhanced methods can create traced timers when trace support is present, such as
`avaje-metrics-otel-trace`.

`@Timed(span = Timed.SpanMode.CHILD)` creates child spans under an existing recording
span, such as a request span created by an HTTP filter. `@Timed(span = Timed.SpanMode.ROOT)`
creates a root span when there is no current recording span, which is useful for
top-level AWS Lambda-style handler methods.

Global default:

```text
timedSpans: default-child
```

Disable all enhanced spans:

```text
timedSpans: disabled
```

Override on a class or method:

```java
@Timed(span = Timed.SpanMode.CHILD)
class BillingService {

  @Timed(span = Timed.SpanMode.ROOT)
  void lambdaHandler() {
  }

  @Timed(span = Timed.SpanMode.OFF)
  void helper() {
  }
}
```

`Timed.SpanMode.DEFAULT` inherits the class-level or `metrics.mf` setting.

---

## Step 8 - Verify

Run:

```bash
mvn process-test-classes
```

This compiles and enhances both `src/main` and `src/test` classes. The Maven console
output shows the timer metrics created by enhancement, which is the quickest way to
confirm enhancement worked as intended.

At runtime, call an enhanced method, then inspect collected metrics:

```java
var metrics = Metrics.collectMetrics();
var json = Metrics.collectAsJson().asJson();
```

Expect timer metrics for the enhanced methods. If nothing appears:

- confirm `metrics-maven-plugin` is configured with `<extensions>true</extensions>`
- if using `metrics.mf`, confirm it is in `src/main/resources` and copied to `target/classes`
- if using `packages`, confirm it matches the application classes
- temporarily set `readOnly: true` and increase `debugLevel`
- check for `@NotTimed` or generated classes that are intentionally skipped
- run `mvn process-test-classes` so both main and test enhancement run after compilation

## Alternative: runtime javaagent

Runtime `-javaagent` enhancement is possible, but it is not the normal Maven path.
Prefer `metrics-maven-plugin` for Maven applications because enhancement happens at
build time and no extra JVM startup argument is required.

## Notes

- Keep `metrics.mf` close to the application that owns the enhancement policy.
- Use `metrics-common.mf` only for shared defaults that should apply across modules.
- Prefer stable, low-cardinality tags.
- Omit `packages` unless you specifically need a package allow-list.
