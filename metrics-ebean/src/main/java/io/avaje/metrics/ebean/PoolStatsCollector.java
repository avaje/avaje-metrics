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
 * <p>Metric shape (when the corresponding pool exists):
 * <ul>
 *   <li>{@code ebean.pool.busy}    — gauge — connections currently in use</li>
 *   <li>{@code ebean.pool.free}    — gauge — connections currently idle</li>
 *   <li>{@code ebean.pool.waiting} — gauge — threads currently waiting for a connection</li>
 *   <li>{@code ebean.pool.acquire} — timer — count = hits, total = totalAcquireMicros, max = maxAcquireMicros</li>
 *   <li>{@code ebean.pool.wait}    — timer — count = waits, total = totalWaitMicros</li>
 * </ul>
 *
 * <p>All metrics carry tags {@code db=<name>, pool=main|readonly}.
 */
final class PoolStatsCollector {

  private final PoolEntry main;
  private final PoolEntry readOnly;

  PoolStatsCollector(Database database) {
    var dbName = database.name();
    DataSource ds = database.dataSource();
    DataSource roDs = database.readOnlyDataSource();
    this.main = (ds instanceof DataSourcePool) ? new PoolEntry((DataSourcePool) ds, dbName, "main") : null;
    this.readOnly = (roDs instanceof DataSourcePool && roDs != ds)
        ? new PoolEntry((DataSourcePool) roDs, dbName, "readonly")
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
    private final Metric.ID busyId;
    private final Metric.ID freeId;
    private final Metric.ID waitingId;
    private final Metric.ID acquireId;
    private final Metric.ID waitId;

    PoolEntry(DataSourcePool pool, String db, String role) {
      this.pool = pool;
      var tags = Tags.of("db:" + db, "pool:" + role);
      this.busyId = Metric.ID.of("ebean.pool.busy", tags);
      this.freeId = Metric.ID.of("ebean.pool.free", tags);
      this.waitingId = Metric.ID.of("ebean.pool.waiting", tags);
      this.acquireId = Metric.ID.of("ebean.pool.acquire", tags);
      this.waitId = Metric.ID.of("ebean.pool.wait", tags);
    }

    void collect(List<Metric.Statistics> out, boolean reset) {
      PoolStatus status = pool.status(reset);
      out.add(new GaugeLongStats(busyId, status.busy()));
      out.add(new GaugeLongStats(freeId, status.free()));
      out.add(new GaugeLongStats(waitingId, status.waiting()));
      out.add(new TimerStats(acquireId, status.hitCount(), status.totalAcquireMicros(), status.maxAcquireMicros()));
      out.add(new TimerStats(waitId, status.waitCount(), status.totalWaitMicros(), 0));
    }
  }
}
