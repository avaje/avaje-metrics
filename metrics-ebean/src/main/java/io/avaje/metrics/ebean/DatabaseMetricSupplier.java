package io.avaje.metrics.ebean;

import io.avaje.applog.AppLog;
import io.avaje.metrics.CollectionMode;
import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricSupplier;
import io.avaje.metrics.stats.CounterStats;
import io.avaje.metrics.stats.TimerStats;
import io.ebean.Database;
import io.ebean.meta.*;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Supplies Ebean metrics to avaje-metrics for reporting.
 *
 * <p>By default emits avaje-metrics names following the label-tag convention:
 * <ul>
 *   <li>{@code ebean.query} with tags {@code type=dto|orm|sql, label=<ebean label>}</li>
 *   <li>{@code ebean.dml}   with tag  {@code label=<ebean label>}</li>
 *   <li>{@code ebean.txn}   with tag  {@code label=<ebean label>}</li>
 *   <li>{@code ebean.l2}    with tags {@code op=..., region=...}</li>
 * </ul>
 *
 * <p>For the older flat-prefixed names (e.g. {@code iud.BProcessLog.insertBatch}) suitable
 * for hierarchical reporters such as Graphite, opt in via {@link #builder(Database)}:
 *
 * <pre>{@code
 * DatabaseMetricSupplier.builder(database)
 *     .legacyNames()
 *     .build();
 * }</pre>
 */
public final class DatabaseMetricSupplier implements MetricSupplier {

  private static final System.Logger log = AppLog.getLogger("io.avaje.metrics.ebean");

  private final Database database;
  private final boolean legacyNames;
  private final PoolStatsCollector poolStats;
  private final ConcurrentMap<String, Metric.ID> idCache = new ConcurrentHashMap<>();

  /**
   * Construct a supplier emitting tagged avaje-metrics names. See {@link #builder(Database)}
   * for the opt-in to legacy flat-prefixed names.
   */
  public DatabaseMetricSupplier(Database database) {
    this(database, false, true, false);
  }

  private DatabaseMetricSupplier(Database database, boolean legacyNames, boolean includePoolMetrics, boolean verbosePoolMetrics) {
    this.database = Objects.requireNonNull(database, "database");
    this.legacyNames = legacyNames;
    this.poolStats = includePoolMetrics ? new PoolStatsCollector(database, verbosePoolMetrics) : null;
  }

  /**
   * Builder for advanced configuration of the supplier.
   */
  public static Builder builder(Database database) {
    return new Builder(database);
  }

  @Override
  public List<Metric.Statistics> collectMetrics() {
    return collectMetrics(CollectionMode.DELTA);
  }

  @Override
  public List<Metric.Statistics> collectMetrics(CollectionMode mode) {
    boolean reset = mode == CollectionMode.DELTA;

    var dbMetrics = new BasicMetricVisitor(database.name(), MetricNamingMatch.INSTANCE, reset, true, true, true);
    database.metaInfo().visitMetrics(dbMetrics);

    List<Metric.Statistics> metrics = new ArrayList<>();
    if (log.isLoggable(Level.DEBUG)) {
      log.log(Level.DEBUG, dbMetrics.asJson().withHash(false).withNewLine(false).json());
    }
    for (MetaTimedMetric timedMetric : dbMetrics.timedMetrics()) {
      metrics.add(new TimerStats(idFor(timedMetric.name()), timedMetric.count(), timedMetric.total(), timedMetric.max()));
    }
    for (MetaQueryMetric metric : dbMetrics.queryMetrics()) {
      metrics.add(new TimerStats(idFor(metric.name()), metric.count(), metric.total(), metric.max()));
    }
    for (MetaCountMetric metric : dbMetrics.countMetrics()) {
      metrics.add(new CounterStats(idFor(metric.name()), metric.count()));
    }
    if (poolStats != null) {
      poolStats.collect(metrics, reset);
    }
    return metrics;
  }

  private Metric.ID idFor(String ebeanName) {
    var cached = idCache.get(ebeanName);
    if (cached != null) {
      return cached;
    }
    var id = legacyNames ? Metric.ID.of(ebeanName) : EbeanMetricNaming.toId(ebeanName);
    idCache.put(ebeanName, id);
    return id;
  }

  /**
   * Builder for {@link DatabaseMetricSupplier}.
   */
  public static final class Builder {

    private final Database database;
    private boolean legacyNames;
    private boolean includePoolMetrics = true;
    private boolean verbosePoolMetrics;

    Builder(Database database) {
      this.database = Objects.requireNonNull(database, "database");
    }

    /**
     * Use the legacy flat-prefixed metric names (e.g. {@code iud.BProcessLog.insertBatch})
     * suitable for hierarchical reporters such as Graphite.
     */
    public Builder legacyNames() {
      this.legacyNames = true;
      return this;
    }

    /**
     * Disable {@link io.ebean.datasource.DataSourcePool} metrics ({@code datasource.pool.*}).
     * <p>By default these are collected when the database's datasource is a
     * {@code DataSourcePool}.
     */
    public Builder excludePoolMetrics() {
      this.includePoolMetrics = false;
      return this;
    }

    /**
     * Emit verbose datasource pool metrics — additionally includes
     * {@code datasource.pool.busy}, {@code .free}, and {@code .waiting} gauges.
     * <p>By default only {@code datasource.pool.size} (busy + free) plus the
     * {@code .acquire} and {@code .wait} timers are emitted.
     */
    public Builder verbosePoolMetrics() {
      this.verbosePoolMetrics = true;
      return this;
    }

    public DatabaseMetricSupplier build() {
      return new DatabaseMetricSupplier(database, legacyNames, includePoolMetrics, verbosePoolMetrics);
    }
  }
}
