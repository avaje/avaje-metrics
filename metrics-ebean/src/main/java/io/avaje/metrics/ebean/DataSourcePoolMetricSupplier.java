package io.avaje.metrics.ebean;

import io.avaje.metrics.CollectionMode;
import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.MetricSupplier;
import io.avaje.metrics.Metrics;
import io.ebean.Database;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Supplies datasource pool metrics for a database without collecting its Ebean metrics.
 *
 * <p>Use this when Ebean metrics are collected separately, such as by
 * {@code InsightClient.collectEbeanMetrics(true)}.
 *
 * <p>Register a default supplier with:
 * <pre>{@code
 * new DataSourcePoolMetricSupplier(database).register();
 * }</pre>
 *
 * <p>Or use the builder to enable verbose metrics before registering:
 * <pre>{@code
 * DataSourcePoolMetricSupplier.builder(database)
 *     .verbosePoolMetrics()
 *     .build()
 *     .register();
 * }</pre>
 */
@NullMarked
public final class DataSourcePoolMetricSupplier implements MetricSupplier {

  private final PoolStatsCollector poolStats;

  /**
   * Construct a supplier for the database's main and read-only datasource pools.
   */
  public DataSourcePoolMetricSupplier(Database database) {
    this(database, false);
  }

  private DataSourcePoolMetricSupplier(Database database, boolean verbosePoolMetrics) {
    poolStats = new PoolStatsCollector(Objects.requireNonNull(database, "database"), verbosePoolMetrics);
  }

  /**
   * Builder for verbose datasource pool metrics.
   */
  public static Builder builder(Database database) {
    return new Builder(database);
  }

  /**
   * Register this supplier with the default metrics registry.
   */
  public DataSourcePoolMetricSupplier register() {
    Metrics.addSupplier(this);
    return this;
  }

  /**
   * Register this supplier with the given metrics registry.
   */
  public DataSourcePoolMetricSupplier register(MetricRegistry registry) {
    registry.addSupplier(this);
    return this;
  }

  @Override
  public List<Metric.Statistics> collectMetrics() {
    return collectMetrics(CollectionMode.DELTA);
  }

  @Override
  public List<Metric.Statistics> collectMetrics(CollectionMode mode) {
    var metrics = new ArrayList<Metric.Statistics>();
    poolStats.collect(metrics, mode == CollectionMode.DELTA);
    return metrics;
  }

  /**
   * Builder for {@link DataSourcePoolMetricSupplier}.
   */
  public static final class Builder {

    private final Database database;
    private boolean verbosePoolMetrics;

    private Builder(Database database) {
      this.database = Objects.requireNonNull(database, "database");
    }

    /**
     * Emit the verbose datasource pool high-water-mark gauge in delta collections.
     */
    public Builder verbosePoolMetrics() {
      verbosePoolMetrics = true;
      return this;
    }

    public DataSourcePoolMetricSupplier build() {
      return new DataSourcePoolMetricSupplier(database, verbosePoolMetrics);
    }
  }
}
