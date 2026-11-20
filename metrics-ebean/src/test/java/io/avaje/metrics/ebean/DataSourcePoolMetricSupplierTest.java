package io.avaje.metrics.ebean;

import io.avaje.metrics.CollectionMode;
import io.avaje.metrics.Metric;
import io.avaje.metrics.stats.GaugeLongStats;
import io.ebean.Database;
import io.ebean.datasource.DataSourcePool;
import io.ebean.datasource.PoolStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataSourcePoolMetricSupplierTest {

  @Test
  void collectsMainAndReadOnlyPoolsWithoutReadingEbeanMetrics() {
    var mainStatus = mock(PoolStatus.class);
    when(mainStatus.busy()).thenReturn(2);
    when(mainStatus.free()).thenReturn(5);
    var readOnlyStatus = mock(PoolStatus.class);
    when(readOnlyStatus.busy()).thenReturn(1);
    when(readOnlyStatus.free()).thenReturn(3);

    var mainPool = mock(DataSourcePool.class);
    when(mainPool.collect(true)).thenReturn(mainStatus);
    var readOnlyPool = mock(DataSourcePool.class);
    when(readOnlyPool.collect(true)).thenReturn(readOnlyStatus);

    var database = mock(Database.class);
    when(database.name()).thenReturn("h2");
    when(database.dataSource()).thenReturn(mainPool);
    when(database.readOnlyDataSource()).thenReturn(readOnlyPool);

    var metrics = new DataSourcePoolMetricSupplier(database).collectMetrics();

    assertThat(metrics).hasSize(2);
    assertThat(metrics)
      .extracting(metric -> metric.id().name())
      .containsExactly("datasource.pool.size", "datasource.pool.size");
    assertThat(metrics)
      .extracting(metric -> metric.id().tags().array()[1])
      .containsExactly("type:main", "type:readonly");
    assertThat(metrics)
      .extracting(metric -> ((GaugeLongStats) metric).value())
      .containsExactly(7L, 4L);
    verify(database, never()).metaInfo();
  }

  @Test
  void cumulativeCollectionDoesNotResetPoolStats() {
    var status = mock(PoolStatus.class);
    when(status.busy()).thenReturn(1);
    when(status.free()).thenReturn(2);

    var pool = mock(DataSourcePool.class);
    when(pool.collect(false)).thenReturn(status);

    var database = mock(Database.class);
    when(database.name()).thenReturn("h2");
    when(database.dataSource()).thenReturn(pool);

    var metrics = DataSourcePoolMetricSupplier.builder(database)
      .verbosePoolMetrics()
      .build()
      .collectMetrics(CollectionMode.CUMULATIVE);

    assertThat(metrics).hasSize(1);
    verify(pool).collect(false);
  }

  @Test
  void noPoolReturnsNoMetrics() {
    var database = mock(Database.class);
    when(database.name()).thenReturn("h2");

    List<Metric.Statistics> metrics = new DataSourcePoolMetricSupplier(database).collectMetrics();

    assertThat(metrics).isEmpty();
    verify(database, never()).metaInfo();
  }
}
