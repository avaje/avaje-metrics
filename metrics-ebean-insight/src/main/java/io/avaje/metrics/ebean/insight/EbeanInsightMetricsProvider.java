package io.avaje.metrics.ebean.insight;

import io.avaje.metrics.CollectionMode;
import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.Metrics;
import io.avaje.metrics.MetricsProvider;
import io.avaje.metrics.ebean.DatabaseMetricSupplier;
import io.avaje.metrics.stats.CounterStats;
import io.avaje.metrics.stats.TimerStats;
import io.ebean.Database;
import io.ebean.insight.InsightClient;
import io.ebean.meta.*;

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
public final class EbeanInsightMetricsProvider implements MetricsProvider {

  private final MetricRegistry registry;
  private final InsightClient insightClient;
  private final Predicate<Metric.Statistics> insightFilter;
  private final Predicate<Metric.Statistics> exportFilter;
  private final DatabaseMetricSupplier databaseSupplier;

  private EbeanInsightMetricsProvider(
    MetricRegistry registry,
    Database database,
    InsightClient insightClient,
    Predicate<Metric.Statistics> insightFilter,
    Predicate<Metric.Statistics> exportFilter) {

    this.registry = Objects.requireNonNull(registry, "registry");
    this.insightClient = Objects.requireNonNull(insightClient, "insightClient");
    this.insightFilter = Objects.requireNonNull(insightFilter, "insightFilter");
    this.exportFilter = Objects.requireNonNull(exportFilter, "exportFilter");
    this.databaseSupplier = DatabaseMetricSupplier.builder(Objects.requireNonNull(database, "database"))
      .build();
  }

  /**
   * Create a provider using the given registry, database, and Insight client.
   */
  public static Builder builder(Database database, InsightClient insightClient) {
    return builder(Metrics.registry(), database, insightClient);
  }

  /**
   * Create a provider using the given registry, database, and Insight client.
   */
  public static Builder builder(MetricRegistry registry, Database database, InsightClient insightClient) {
    return new Builder(registry, database, insightClient);
  }

  private BasicMetricVisitor dbmetrics(Database database, CollectionMode mode) {
    boolean reset = mode == CollectionMode.DELTA;

    var dbMetrics = new BasicMetricVisitor(database.name(), MetricNamingMatch.INSTANCE, reset, true, true, true);
    database.metaInfo().visitMetrics(dbMetrics);
    return dbMetrics;
  }

  @Override
  public List<Metric.Statistics> provide(CollectionMode mode) {
    List<Metric.Statistics> initialMetrics = new ArrayList<>(registry.collectMetrics(mode));

    var dbMetrics = databaseSupplier.collect(mode);
    initialMetrics.addAll(dbMetrics.poolMetrics());

    var insightMetrics = initialMetrics.stream()
      .filter(insightFilter)
      .collect(Collectors.toList());

    // send to ebean insight server
    insightClient.accept(dbMetrics.serverMetrics(), insightMetrics);

    return initialMetrics.stream()
      .filter(exportFilter)
      .collect(Collectors.toList());
  }


  /**
   * Builder for {@link EbeanInsightMetricsProvider}.
   */
  public static final class Builder {

    private final MetricRegistry registry;
    private final Database database;
    private final InsightClient insightClient;
    private Predicate<Metric.Statistics> insightFilter = statistics -> true;
    private Predicate<Metric.Statistics> exportFilter = statistics -> true;

    private Builder(MetricRegistry registry, Database database, InsightClient insightClient) {
      this.registry = Objects.requireNonNull(registry, "registry");
      this.database = Objects.requireNonNull(database, "database");
      this.insightClient = Objects.requireNonNull(insightClient, "insightClient");
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
    public EbeanInsightMetricsProvider build() {
      return new EbeanInsightMetricsProvider(
        registry, database, insightClient, insightFilter, exportFilter);
    }
  }
}
