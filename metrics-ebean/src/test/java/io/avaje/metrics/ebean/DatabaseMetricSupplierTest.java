package io.avaje.metrics.ebean;

import io.avaje.metrics.Metric;
import io.ebean.Database;
import io.ebean.datasource.DataSourcePool;
import io.ebean.datasource.PoolStatus;
import io.ebean.meta.MetaInfoManager;
import io.ebean.meta.ServerMetrics;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseMetricSupplierTest {

  @Test
  void forwardSnapshot_includesPoolMetricsFromSameCollection() {
    var status = mock(PoolStatus.class);
    when(status.busy()).thenReturn(3);
    when(status.free()).thenReturn(7);
    when(status.waiting()).thenReturn(0);
    when(status.hitCount()).thenReturn(0);
    when(status.waitCount()).thenReturn(0);
    when(status.highWaterMark()).thenReturn(3);

    var pool = mock(DataSourcePool.class);
    when(pool.status(true)).thenReturn(status);

    var metaInfo = mock(MetaInfoManager.class);
    var database = mock(Database.class);
    when(database.name()).thenReturn("h2");
    when(database.metaInfo()).thenReturn(metaInfo);
    when(database.dataSource()).thenReturn(pool);
    when(database.readOnlyDataSource()).thenReturn(null);

    var ebeanSnapshots = new ArrayList<ServerMetrics>();
    var avajeSnapshots = new ArrayList<List<Metric.Statistics>>();
    var supplier = DatabaseMetricSupplier.builder(database)
      .forwardTo(ebeanSnapshots::add)
      .forwardSnapshotTo((ebean, avaje) -> {
        ebeanSnapshots.add(ebean);
        avajeSnapshots.add(avaje);
      })
      .build();

    var collected = supplier.collectMetrics();

    assertThat(ebeanSnapshots).hasSize(2);
    assertThat(avajeSnapshots).hasSize(1);
    assertThat(avajeSnapshots.get(0)).containsExactlyElementsOf(collected);
    assertThat(avajeSnapshots.get(0))
      .extracting(statistics -> statistics.id().name())
      .containsExactly("datasource.pool.size");
  }
}
