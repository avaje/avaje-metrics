package io.avaje.metrics.ebean;

import io.avaje.applog.AppLog;
import io.avaje.metrics.*;
import io.avaje.metrics.stats.CounterStats;
import io.avaje.metrics.stats.TimerStats;
import io.ebean.Database;
import io.ebean.meta.*;
import org.jspecify.annotations.NonNull;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Supplies Ebean metrics to avaje-metrics for reporting.
 *
 * <p>By default emits avaje-metrics names following the label-tag convention:
 * <ul>
 *   <li>{@code ebean.query} with tags {@code kind=dto|orm|sql, type=<bean>, label=<ebean label>}</li>
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
 *     .build()
 *     .register(); // usually register with the default registry
 * }</pre>
 */
public final class DatabaseMetricSupplier implements MetricSupplier {

  private static final System.Logger log = AppLog.getLogger("io.avaje.metrics.ebean");

  private final Database database;
  private final boolean legacyNames;
  private final PoolStatsCollector poolStats;
  private final Consumer<ServerMetrics> forwardTo;
  private final BiConsumer<ServerMetrics, List<Metric.Statistics>> forwardSnapshotTo;
  private final ConcurrentMap<String, Metric.ID> idCache = new ConcurrentHashMap<>();

  /**
   * Construct a supplier emitting tagged avaje-metrics names. See {@link #builder(Database)}
   * for the opt-in to legacy flat-prefixed names.
   */
  public DatabaseMetricSupplier(Database database) {
    this(database, false, true, false, null, null);
  }

  private DatabaseMetricSupplier(Database database, boolean legacyNames, boolean includePoolMetrics,
                                 boolean verbosePoolMetrics, Consumer<ServerMetrics> forwardTo,
                                 BiConsumer<ServerMetrics, List<Metric.Statistics>> forwardSnapshotTo) {
    this.database = Objects.requireNonNull(database, "database");
    this.legacyNames = legacyNames;
    this.poolStats = includePoolMetrics ? new PoolStatsCollector(database, verbosePoolMetrics) : null;
    this.forwardTo = forwardTo;
    this.forwardSnapshotTo = forwardSnapshotTo;
  }

  /**
   * Builder for advanced configuration of the supplier.
   */
  public static Builder builder(Database database) {
    return new Builder(database);
  }

  /**
   * Register this as a MetricSupplier to the default Metrics registry.
   * See {@link #register(MetricRegistry)} for registration to a custom registry.
   *
   * @return This MetricSupplier
   */
  public DatabaseMetricSupplier register() {
    Metrics.addSupplier(this);
    return this;
  }

  /**
   * Register this MetricSupplier to the MetricRegistry.
   *
   * @return This MetricSupplier
   */
  public DatabaseMetricSupplier register(MetricRegistry registry) {
    registry.addSupplier(this);
    return this;
  }

  @Override
  public List<Metric.Statistics> collectMetrics() {
    return collectMetrics(CollectionMode.DELTA);
  }

  @Override
  public List<Metric.Statistics> collectMetrics(CollectionMode mode) {
    boolean reset = mode == CollectionMode.DELTA;
    var databaseMetrics = collect(mode);

    if (forwardTo != null) {
      try {
        // forward to an external consumer (e.g. ebean-insight)
        forwardTo.accept(databaseMetrics.serverMetrics());
      } catch (Throwable e) {
        log.log(Level.WARNING, "forwardTo consumer threw", e);
      }
    }

    if (forwardSnapshotTo != null && reset) {
      try {
        // forward the already-collected Avaje metrics without polling again
        forwardSnapshotTo.accept(databaseMetrics.serverMetrics(), databaseMetrics.poolMetrics());
      } catch (Throwable e) {
        log.log(Level.WARNING, "forwardSnapshotTo consumer threw", e);
      }
    }
    List<Metric.Statistics> metrics = convert(databaseMetrics.serverMetrics());
    metrics.addAll(databaseMetrics.poolMetrics());
    return metrics;
  }

  @NonNull
  public DatabaseMetrics collect(CollectionMode mode) {
    boolean reset = mode == CollectionMode.DELTA;
    var dbMetrics = new BasicMetricVisitor(database.name(), MetricNamingMatch.INSTANCE, reset, true, true, true);
    database.metaInfo().visitMetrics(dbMetrics);

    return new DatabaseMetrics(dbMetrics, poolMetrics(reset));
  }

  @NonNull
  private List<Metric.Statistics> poolMetrics(boolean reset) {
    if (poolStats == null) {
      return List.of();
    }
    List<Metric.Statistics> poolMetrics = new ArrayList<>();
    poolStats.collect(poolMetrics, reset);
    return poolMetrics;
  }

  @NonNull
  public List<Metric.Statistics> convert(ServerMetrics dbMetrics) {
    List<Metric.Statistics> metrics = new ArrayList<>();
    if (log.isLoggable(Level.DEBUG)) {
      log.log(Level.DEBUG, dbMetrics.asJson().withHash(false).withNewLine(false).json());
    }
    for (MetaTimedMetric timedMetric : dbMetrics.timedMetrics()) {
      metrics.add(new TimerStats(idFor(timedMetric.name()), timedMetric.count(), timedMetric.total(), timedMetric.max()));
    }
    for (MetaQueryMetric metric : dbMetrics.queryMetrics()) {
      metrics.add(new TimerStats(idForQuery(metric), metric.count(), metric.total(), metric.max()));
    }
    for (MetaCountMetric metric : dbMetrics.countMetrics()) {
      metrics.add(new CounterStats(idFor(metric.name()), metric.count()));
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

  private Metric.ID idForQuery(MetaQueryMetric metric) {
    var ebeanName = metric.name();
    Class<?> beanType = metric.type();
    var beanTypeName = beanType == null ? null : beanType.getSimpleName();
    // key by name + bean type: two plans can share a name (e.g. same profile
    // location, different bean type) yet must map to distinct ids/tags
    var cacheKey = beanTypeName == null ? ebeanName : ebeanName + '\t' + beanTypeName;
    var cached = idCache.get(cacheKey);
    if (cached != null) {
      return cached;
    }
    var id = legacyNames ? Metric.ID.of(ebeanName) : EbeanMetricNaming.toId(ebeanName, beanTypeName);
    idCache.put(cacheKey, id);
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
    private Consumer<ServerMetrics> forwardTo;
    private BiConsumer<ServerMetrics, List<Metric.Statistics>> forwardSnapshotTo;

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
     * Emit verbose datasource pool metrics — additionally includes the
     * {@code datasource.pool.busyHwm} gauge (peak busy connections since the last
     * reset), emitted when metrics are collected in reset/delta mode.
     * <p>By default only {@code datasource.pool.size} (busy + free) plus the
     * {@code .acquire} and {@code .wait} timers are emitted.
     */
    public Builder verbosePoolMetrics() {
      this.verbosePoolMetrics = true;
      return this;
    }

    /**
     * Forward each collected {@link ServerMetrics} snapshot to the given
     * consumer, in addition to translating it for avaje-metrics. Lets a single
     * upstream collector own the reset-on-read poll while sharing the snapshot
     * with another sink (e.g. {@code InsightClient} from ebean-insight-client).
     * Invoked for every collection mode.
     */
    public Builder forwardTo(Consumer<ServerMetrics> forwardTo) {
      this.forwardTo = forwardTo;
      return this;
    }

    /**
     * Forward each reset-on-read Ebean snapshot together with the already-collected
     * Avaje statistics, including datasource pool metrics, to the given consumer.
     * This avoids a second collection that could otherwise split or reset the
     * reporting interval. Only invoked in {@code DELTA} mode.
     */
    public Builder forwardSnapshotTo(BiConsumer<ServerMetrics, List<Metric.Statistics>> forwardSnapshotTo) {
      this.forwardSnapshotTo = forwardSnapshotTo;
      return this;
    }

    public DatabaseMetricSupplier build() {
      return new DatabaseMetricSupplier(database, legacyNames, includePoolMetrics, verbosePoolMetrics,
        forwardTo, forwardSnapshotTo);
    }
  }

  public static final class DatabaseMetrics {

    private final ServerMetrics serverMetrics;
    private final List<Metric.Statistics> poolMetrics;

    public DatabaseMetrics(ServerMetrics serverMetrics, List<Metric.Statistics> poolMetrics) {
      this.serverMetrics = serverMetrics;
      this.poolMetrics = poolMetrics;
    }

    public ServerMetrics serverMetrics() {
      return serverMetrics;
    }

    public List<Metric.Statistics> poolMetrics() {
      return poolMetrics;
    }
  }
}
