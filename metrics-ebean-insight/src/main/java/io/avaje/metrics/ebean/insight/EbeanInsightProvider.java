package io.avaje.metrics.ebean.insight;

import io.avaje.metrics.CollectionMode;
import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.Metrics;
import io.avaje.metrics.MetricsProvider;
import io.avaje.metrics.ebean.DatabaseMetricSupplier;
import io.ebean.Database;
import io.ebean.insight.InsightClient;
import io.ebean.meta.ServerMetrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Provides filtered Avaje metrics while forwarding Ebean metrics and a filtered
 * Avaje snapshot to an {@link InsightClient}.
 *
 * <p>The provider owns an unregistered Ebean metric supplier. Registry
 * configuration remains the responsibility of the caller.
 */
public final class EbeanInsightProvider implements MetricsProvider {

  private final MetricRegistry registry;
  private final InsightClient insightClient;
  private final Predicate<Metric.Statistics> insightFilter;
  private final Predicate<Metric.Statistics> exportFilter;
  private final List<DatabaseMetricSupplier> databaseSuppliers;

  private EbeanInsightProvider(
    MetricRegistry registry,
    List<Database> databases,
    InsightClient insightClient,
    Predicate<Metric.Statistics> insightFilter,
    Predicate<Metric.Statistics> exportFilter) {

    this.registry = Objects.requireNonNull(registry, "registry");
    Objects.requireNonNull(databases, "databases");
    if (databases.isEmpty()) {
      throw new IllegalArgumentException("at least one database is required");
    }
    this.insightClient = Objects.requireNonNull(insightClient, "insightClient");
    this.insightFilter = Objects.requireNonNull(insightFilter, "insightFilter");
    this.exportFilter = Objects.requireNonNull(exportFilter, "exportFilter");
    this.databaseSuppliers = databases.stream()
      .map(database -> DatabaseMetricSupplier.builder(
        Objects.requireNonNull(database, "database")).build())
      .collect(Collectors.toList());
  }

  /**
   * Create a provider using the default registry, database, and Insight client.
   */
  public static Builder builder(InsightClient insightClient, Database database) {
    return new Builder(Metrics.registry(), insightClient).database(database);
  }

  /**
   * Create a provider builder using the default registry.
   */
  public static Builder builder(InsightClient insightClient) {
    return new Builder(Metrics.registry(), insightClient);
  }

  @Override
  public List<Metric.Statistics> provide(CollectionMode mode) {
    List<Metric.Statistics> registryMetrics = registry.collectMetrics(mode);
    List<Metric.Statistics> insightProjection = new ArrayList<>(registryMetrics);
    List<Metric.Statistics> exportProjection = new ArrayList<>(registryMetrics);
    List<ServerMetrics> databaseSnapshots = new ArrayList<>(databaseSuppliers.size());

    for (DatabaseMetricSupplier databaseSupplier : databaseSuppliers) {
      var databaseMetrics = databaseSupplier.collect(mode);
      databaseSnapshots.add(databaseMetrics.serverMetrics());
      insightProjection.addAll(databaseMetrics.poolMetrics());
      exportProjection.addAll(databaseSupplier.convert(databaseMetrics.serverMetrics()));
      exportProjection.addAll(databaseMetrics.poolMetrics());
    }

    var insightMetrics = insightProjection.stream()
      .filter(insightFilter)
      .collect(Collectors.toList());

    insightClient.sendNow(insightMetrics, databaseSnapshots);

    return exportProjection.stream()
      .filter(exportFilter)
      .collect(Collectors.toList());
  }


  /**
   * Builder for {@link EbeanInsightProvider}.
   */
  public static final class Builder {

    private final MetricRegistry registry;
    private final InsightClient insightClient;
    private final List<Database> databases = new ArrayList<>();
    private Predicate<Metric.Statistics> insightFilter = statistics -> true;
    private Predicate<Metric.Statistics> exportFilter = statistics -> true;

    private Builder(MetricRegistry registry, InsightClient insightClient) {
      this.registry = Objects.requireNonNull(registry, "registry");
      this.insightClient = Objects.requireNonNull(insightClient, "insightClient");
    }

    /**
     * Add a database whose metrics should be collected and sent to Insight.
     */
    public Builder database(Database database) {
      databases.add(Objects.requireNonNull(database, "database"));
      return this;
    }

    /**
     * Add all databases whose metrics should be collected and sent to Insight.
     */
    public Builder databases(List<Database> databases) {
      for (Database database : Objects.requireNonNull(databases, "databases")) {
        database(database);
      }
      return this;
    }

    /**
     * Select the metrics sent to Insight alongside the raw Ebean metrics.
     */
    public Builder insightFilter(Predicate<Metric.Statistics> insightFilter) {
      this.insightFilter = Objects.requireNonNull(insightFilter, "insightFilter");
      return this;
    }

    /**
     * Select the metrics returned to the OTEL or StatsD reporter.
     */
    public Builder exportFilter(Predicate<Metric.Statistics> exportFilter) {
      this.exportFilter = Objects.requireNonNull(exportFilter, "exportFilter");
      return this;
    }

    /**
     * Build the provider without changing the registry configuration.
     */
    public EbeanInsightProvider build() {
      return new EbeanInsightProvider(
        registry, List.copyOf(databases), insightClient, insightFilter, exportFilter);
    }
  }
}
