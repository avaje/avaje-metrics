package io.avaje.metrics.ebean;

import io.avaje.metrics.Metric;
import io.avaje.metrics.Tags;
import io.avaje.metrics.stats.GaugeLongStats;
import io.avaje.metrics.stats.TimerStats;
import io.ebean.Database;
import io.ebean.datasource.DataSourcePool;
import io.ebean.datasource.PoolStatus;

import javax.sql.DataSource;
import java.util.List;

/**
 * Collects {@link DataSourcePool} metrics for a {@link Database}, when its main
 * and/or read-only datasource is a {@code DataSourcePool}.
 *
 * <p>Default (normal) metric shape:
 * <ul>
 *   <li>{@code datasource.pool.size}    — gauge — busy + free</li>
 *   <li>{@code datasource.pool.acquire} — timer — count = hits, total = totalAcquireMicros, max = maxAcquireMicros</li>
 *   <li>{@code datasource.pool.wait}    — timer — count = waits, total = totalWaitMicros</li>
 * </ul>
 *
 * <p>Verbose mode additionally emits:
 * <ul>
 *   <li>{@code datasource.pool.busy}    — gauge — connections currently in use</li>
 *   <li>{@code datasource.pool.free}    — gauge — connections currently idle</li>
 *   <li>{@code datasource.pool.waiting} — gauge — threads currently waiting for a connection</li>
 * </ul>
 *
 * <p>All metrics carry tags {@code db=<name>, type=main|readonly}.
 */
final class PoolStatsCollector {

  private final PoolEntry main;
  private final PoolEntry readOnly;

  PoolStatsCollector(Database database, boolean verbose) {
    var dbName = database.name();
    DataSource ds = database.dataSource();
    DataSource roDs = database.readOnlyDataSource();
    this.main = (ds instanceof DataSourcePool) ? new PoolEntry((DataSourcePool) ds, dbName, "main", verbose) : null;
    this.readOnly = (roDs instanceof DataSourcePool && roDs != ds)
        ? new PoolEntry((DataSourcePool) roDs, dbName, "readonly", verbose)
        : null;
  }

  boolean hasAny() {
    return main != null || readOnly != null;
  }

  void collect(List<Metric.Statistics> out, boolean reset) {
    if (main != null) main.collect(out, reset);
    if (readOnly != null) readOnly.collect(out, reset);
  }

  /** Pre-built Metric.IDs and stats fetch for a single pool. */
  private static final class PoolEntry {

    private final DataSourcePool pool;
    private final boolean verbose;
    private final Metric.ID sizeId;
    private final Metric.ID busyId;
    private final Metric.ID freeId;
    private final Metric.ID waitingId;
    private final Metric.ID acquireId;
    private final Metric.ID waitId;

    PoolEntry(DataSourcePool pool, String db, String role, boolean verbose) {
      this.pool = pool;
      this.verbose = verbose;
      var tags = Tags.of("db:" + db, "type:" + role);
      this.sizeId = Metric.ID.of("datasource.pool.size", tags);
      this.acquireId = Metric.ID.of("datasource.pool.acquire", tags);
      this.waitId = Metric.ID.of("datasource.pool.wait", tags);
      if (verbose) {
        this.busyId = Metric.ID.of("datasource.pool.busy", tags);
        this.freeId = Metric.ID.of("datasource.pool.free", tags);
        this.waitingId = Metric.ID.of("datasource.pool.waiting", tags);
      } else {
        this.busyId = null;
        this.freeId = null;
        this.waitingId = null;
      }
    }

    void collect(List<Metric.Statistics> out, boolean reset) {
      PoolStatus status = pool.status(reset);
      int busy = status.busy();
      int free = status.free();
      out.add(new GaugeLongStats(sizeId, busy + free));
      if (verbose) {
        out.add(new GaugeLongStats(busyId, busy));
        out.add(new GaugeLongStats(freeId, free));
        out.add(new GaugeLongStats(waitingId, status.waiting()));
      }
      out.add(new TimerStats(acquireId, status.hitCount(), status.totalAcquireMicros(), status.maxAcquireMicros()));
      out.add(new TimerStats(waitId, status.waitCount(), status.totalWaitMicros(), 0));
    }
  }
}
