package io.avaje.metrics.statsd;

import com.timgroup.statsd.StatsDClient;
import io.ebean.Database;
import io.ebean.datasource.DataSourcePool;
import io.ebean.meta.MetaCountMetric;
import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.meta.ServerMetrics;

import javax.sql.DataSource;

import static io.avaje.metrics.statsd.Trim.qry;

final class DatabaseReporter implements StatsdReporter.Reporter {

  private final Database database;

  private DatabaseReporter(Database database) {
    this.database = database;
  }

  static StatsdReporter.Reporter reporter(Database database) {
    return new DatabaseReporter(database);
  }

  @Override
  public void report(StatsDClient sender) {
    new DReport(database, sender).report();
  }

  private static final class DReport {

    private final StatsDClient reporter;
    private final ServerMetrics dbMetrics;
    private final long epochSecs;
    private final String dbTag;

    private DReport(Database database, StatsDClient reporter) {
      this.reporter = reporter;
      this.dbTag = "db:" + database.name();
      this.dbMetrics = database.metaInfo().collectMetrics();
      this.epochSecs = System.currentTimeMillis() / 1000;
      DataSource dataSource = database.dataSource();
      if (dataSource instanceof DataSourcePool) {
        DataSourcePool pool = (DataSourcePool) dataSource;
        reporter.gaugeWithTimestamp("db.pool.size", pool.size(), epochSecs, dbTag, "type:main");
      }
      DataSource readDatasource = database.readOnlyDataSource();
      if (readDatasource instanceof DataSourcePool && readDatasource != dataSource) {
        DataSourcePool readOnlyPool = (DataSourcePool) readDatasource;
        reporter.gaugeWithTimestamp("db.pool.size", readOnlyPool.size(), epochSecs,dbTag, "type:readonly");
      }
    }

    private void report() {
      for (MetaTimedMetric timedMetric : dbMetrics.timedMetrics()) {
        reportTimedMetric(timedMetric);
      }
      for (MetaQueryMetric queryMetric : dbMetrics.queryMetrics()) {
        reportQueryMetric(queryMetric);
      }
      for (MetaCountMetric countMetric : dbMetrics.countMetrics()) {
        reportCountMetric(countMetric);
      }
    }

    private void reportCountMetric(MetaCountMetric countMetric) {
      reporter.count(nm("db.count.", countMetric.name()), countMetric.count(), dbTag);
    }

    private void reportTimedMetric(MetaTimedMetric metric) {
      final String name = metric.name();
      if (name.startsWith("txn.")) {
        reportMetric(metric, "db.txn", dbTag, "name:" + qry(name));
      } else {
        reportMetric(metric, nm("db.timed.", name), dbTag);
      }
    }

    private void reportQueryMetric(MetaQueryMetric metric) {
      final var name = metric.name();
      final var queryTag = "name:" + qry(name);
      if (name.startsWith("orm")) {
        reportMetric(metric, "db.query", dbTag, queryTag, "type:orm");
      } else if (name.startsWith("sql")) {
        reportMetric(metric, "db.query", dbTag, queryTag, "type:sql");
      } else {
        reportMetric(metric, "db.query", dbTag, queryTag, "type:other");
      }
    }

    private void reportMetric(MetaTimedMetric metric, String name, String... tags) {
      reporter.countWithTimestamp(nm(name, ".count"), metric.count(), epochSecs, tags);
      reporter.gaugeWithTimestamp(nm(name, ".max"), metric.max(), epochSecs, tags);
      reporter.gaugeWithTimestamp(nm(name, ".mean"), metric.mean(), epochSecs, tags);
      reporter.gaugeWithTimestamp(nm(name, ".total"), metric.total(), epochSecs, tags);
    }

    private String nm(String name, String suffix) {
      return name + suffix;
    }
  }

}
