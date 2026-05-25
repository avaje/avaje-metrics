# avaje-metrics-ebean

Supplies Ebean database metrics to avaje-metrics via `DatabaseMetricSupplier`.

## Maven dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics-ebean</artifactId>
  <version>${version}</version>
</dependency>
```

If the application uses `module-info.java`, also add:

```java
requires io.avaje.metrics.ebean;
```

## Basic usage

```java
import io.avaje.metrics.Metrics;
import io.avaje.metrics.ebean.DatabaseMetricSupplier;

Metrics.addSupplier(new DatabaseMetricSupplier(database));
```

Once added, normal registry collection and export paths will see the database metrics:

```java
var metrics = Metrics.collectMetrics();
```

## Notes

- `DatabaseMetricSupplier` supports both delta and cumulative collection modes.
- This module is useful with registry-driven export paths such as manual collection or
  `avaje-metrics-otel-producer`.
- If you are using `avaje-metrics-statsd` or `avaje-metrics-graphite`, those modules also
  provide direct `.database(...)` convenience methods on their reporter builders.

## More docs

- [Guide index](../docs/guides/README.md)
- [Getting started](../docs/guides/getting-started.md)
- [Add Ebean metrics](../docs/guides/add-ebean-metrics.md)
