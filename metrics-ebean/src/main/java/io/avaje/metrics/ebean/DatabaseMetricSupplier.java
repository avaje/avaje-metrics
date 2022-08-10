package io.avaje.metrics.ebean;

import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricSupplier;
import io.avaje.metrics.stats.CounterStats;
import io.avaje.metrics.stats.TimerStats;
import io.ebean.Database;
import io.ebean.meta.MetaCountMetric;
import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.meta.ServerMetrics;

import java.util.ArrayList;
import java.util.List;

public class DatabaseMetricSupplier implements MetricSupplier {

  final Database database;

  public DatabaseMetricSupplier(Database database) {
    this.database = database;
  }

  @Override
  public List<Metric.Statistics> collectMetrics() {
    List<Metric.Statistics> metrics = new ArrayList<>();
    ServerMetrics dbMetrics = database.metaInfo().collectMetrics();
    //log.info("dbMetrics {} {} {} {}", database.name(), dbMetrics.timedMetrics(), dbMetrics.queryMetrics(), dbMetrics.countMetrics());
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
