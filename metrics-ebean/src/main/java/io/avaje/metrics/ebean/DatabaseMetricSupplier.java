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

/**
 * Supplies Ebean metrics to avaje-metrics for reporting.
 */
public final class DatabaseMetricSupplier implements MetricSupplier {

  private static final System.Logger log = AppLog.getLogger("io.avaje.metrics.ebean");

  final Database database;

  public DatabaseMetricSupplier(Database database) {
    this.database = database;
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
      metrics.add(new TimerStats(Metric.ID.of(timedMetric.name()), timedMetric.count(), timedMetric.total(), timedMetric.max()));
    }
    for (MetaQueryMetric metric : dbMetrics.queryMetrics()) {
      metrics.add(new TimerStats(Metric.ID.of(metric.name()), metric.count(), metric.total(), metric.max()));
    }
    for (MetaCountMetric metric : dbMetrics.countMetrics()) {
      metrics.add(new CounterStats(Metric.ID.of(metric.name()), metric.count()));
    }
    return metrics;
  }

}
