package io.avaje.metrics.ebean;

import io.avaje.metrics.Metric;
import io.avaje.metrics.stats.GaugeLongStats;
import io.avaje.metrics.stats.TimerStats;
import io.ebean.Database;
import io.ebean.datasource.DataSourcePool;
import io.ebean.datasource.PoolStatus;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PoolStatsCollectorTest {

  private static PoolStatus status(int busy, int free, int waiting,
                                   int hits, long totalAcquireMicros,
                                   int waits, long totalWaitMicros,
                                   long maxAcquireMicros) {
    var s = mock(PoolStatus.class);
    when(s.busy()).thenReturn(busy);
    when(s.free()).thenReturn(free);
    when(s.waiting()).thenReturn(waiting);
    when(s.hitCount()).thenReturn(hits);
    when(s.totalAcquireMicros()).thenReturn(totalAcquireMicros);
    when(s.waitCount()).thenReturn(waits);
    when(s.totalWaitMicros()).thenReturn(totalWaitMicros);
    when(s.maxAcquireMicros()).thenReturn(maxAcquireMicros);
    return s;
  }

  @Test
  void mainPoolOnly_emitsAllMetrics() {
    var s = status(3, 7, 1, 100, 250_000L, 5, 1_000L, 8_000L);
    var pool = mock(DataSourcePool.class);
    when(pool.status(true)).thenReturn(s);

    var db = mock(Database.class);
    when(db.name()).thenReturn("h2");
    when(db.dataSource()).thenReturn(pool);
    when(db.readOnlyDataSource()).thenReturn(null);

    var collector = new PoolStatsCollector(db);
    assertThat(collector.hasAny()).isTrue();

    var out = new ArrayList<Metric.Statistics>();
    collector.collect(out, true);

    assertThat(out).hasSize(5);

    var byName = byName(out);
    assertThat(((GaugeLongStats) byName.get("ebean.pool.busy")).id().tags().array())
        .containsExactly("db:h2", "pool:main");

    var acquire = (TimerStats) byName.get("ebean.pool.acquire");
    assertThat(acquire.count()).isEqualTo(100);
    assertThat(acquire.total()).isEqualTo(250_000L);
    assertThat(acquire.max()).isEqualTo(8_000L);

    var wait = (TimerStats) byName.get("ebean.pool.wait");
    assertThat(wait.count()).isEqualTo(5);
    assertThat(wait.total()).isEqualTo(1_000L);
  }

  @Test
  void noPool_collectsNothing() {
    var ds = mock(DataSource.class); // not a DataSourcePool
    var db = mock(Database.class);
    when(db.name()).thenReturn("h2");
    when(db.dataSource()).thenReturn(ds);
    when(db.readOnlyDataSource()).thenReturn(ds);

    var collector = new PoolStatsCollector(db);
    assertThat(collector.hasAny()).isFalse();

    var out = new ArrayList<Metric.Statistics>();
    collector.collect(out, true);
    assertThat(out).isEmpty();
  }

  @Test
  void samePoolForMainAndReadOnly_emitsOnce() {
    var s = status(1, 1, 0, 1, 0L, 0, 0L, 0L);
    var pool = mock(DataSourcePool.class);
    when(pool.status(true)).thenReturn(s);

    var db = mock(Database.class);
    when(db.name()).thenReturn("h2");
    when(db.dataSource()).thenReturn(pool);
    when(db.readOnlyDataSource()).thenReturn(pool);

    var out = new ArrayList<Metric.Statistics>();
    new PoolStatsCollector(db).collect(out, true);

    var roleTags = out.stream()
        .map(stat -> stat.id().tags().array()[1])
        .distinct()
        .collect(java.util.stream.Collectors.toList());
    assertThat(roleTags).containsExactly("pool:main");
  }

  @Test
  void mainAndReadOnlyPools_bothEmitted() {
    var s1 = status(0, 0, 0, 0, 0L, 0, 0L, 0L);
    var s2 = status(0, 0, 0, 0, 0L, 0, 0L, 0L);
    var mainPool = mock(DataSourcePool.class);
    when(mainPool.status(true)).thenReturn(s1);
    var roPool = mock(DataSourcePool.class);
    when(roPool.status(true)).thenReturn(s2);

    var db = mock(Database.class);
    when(db.name()).thenReturn("h2");
    when(db.dataSource()).thenReturn(mainPool);
    when(db.readOnlyDataSource()).thenReturn(roPool);

    var out = new ArrayList<Metric.Statistics>();
    new PoolStatsCollector(db).collect(out, true);

    // 5 metrics × 2 pools
    assertThat(out).hasSize(10);
    assertThat(out.stream().map(stat -> stat.id().tags().array()[1]).distinct()
        .collect(java.util.stream.Collectors.toList()))
        .containsExactlyInAnyOrder("pool:main", "pool:readonly");
  }

  @Test
  void cumulativeMode_passesResetFalse() {
    var s = status(0, 0, 0, 0, 0L, 0, 0L, 0L);
    var pool = mock(DataSourcePool.class);
    when(pool.status(false)).thenReturn(s);

    var db = mock(Database.class);
    when(db.name()).thenReturn("h2");
    when(db.dataSource()).thenReturn(pool);
    when(db.readOnlyDataSource()).thenReturn(null);

    new PoolStatsCollector(db).collect(new ArrayList<>(), false);
    // verified via the when(pool.status(false)) stub being matched
  }

  private static java.util.Map<String, Metric.Statistics> byName(List<Metric.Statistics> stats) {
    var m = new java.util.HashMap<String, Metric.Statistics>();
    for (var s : stats) {
      m.put(s.id().name(), s);
    }
    return m;
  }
}
