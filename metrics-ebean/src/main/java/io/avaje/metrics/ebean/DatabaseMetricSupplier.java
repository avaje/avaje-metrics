package io.avaje.metrics.ebean;

import io.avaje.applog.AppLog;
import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricSupplier;
import io.avaje.metrics.stats.CounterStats;
import io.avaje.metrics.stats.TimerStats;
import io.ebean.Database;
import io.ebean.meta.MetaCountMetric;
import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.meta.ServerMetrics;

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
    List<Metric.Statistics> metrics = new ArrayList<>();
    ServerMetrics dbMetrics = database.metaInfo().collectMetrics();
    if (log.isLoggable(Level.DEBUG)) {
      log.log(Level.DEBUG, dbMetrics.asJson().withHash(false).withNewLine(false).json());
    }
    for (MetaTimedMetric timedMetric : dbMetrics.timedMetrics()) {
      metrics.add(new TimerStats(timedMetric.name(), timedMetric.count(), timedMetric.total(), timedMetric.max()));
    }
    for (MetaQueryMetric metric : dbMetrics.queryMetrics()) {
      metrics.add(new TimerStats(metric.name(), metric.count(), metric.total(), metric.max()));
    }
    for (MetaCountMetric metric : dbMetrics.countMetrics()) {
      metrics.add(new CounterStats(metric.name(), metric.count()));
    }
    return metrics;
  }

}
