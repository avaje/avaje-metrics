package io.avaje.metrics.statsd;

import com.timgroup.statsd.StatsDClient;
import io.ebean.Database;
import io.ebean.datasource.DataSourcePool;
import io.ebean.meta.MetaCountMetric;
import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.meta.ServerMetrics;

import javax.sql.DataSource;

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
    private final String dbName;

    private DReport(Database database, StatsDClient reporter) {
      this.reporter = reporter;
      this.dbName = database.name();
      this.dbMetrics = database.metaInfo().collectMetrics();
      this.epochSecs = System.currentTimeMillis() / 1000;
      DataSource dataSource = database.dataSource();
      if (dataSource instanceof DataSourcePool) {
        DataSourcePool pool = (DataSourcePool) database;
        reporter.gaugeWithTimestamp("pool.size", pool.size(), epochSecs, "db", dbName, "type", "main");
      }
      DataSource readDatasource = database.readOnlyDataSource();
      if (readDatasource instanceof DataSourcePool && readDatasource != dataSource) {
        DataSourcePool readOnlyPool = (DataSourcePool) readDatasource;
        reporter.gaugeWithTimestamp("pool.size", readOnlyPool.size(), epochSecs,"db", dbName, "type", "readonly");
      }
    }

    private String nm(String name, String suffix) {
      return name + suffix;
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
      reporter.count(nm(countMetric.name(), ".count"), countMetric.count(), "db", dbName);
    }

    private void reportTimedMetric(MetaTimedMetric metric) {
      final String name = metric.name();
      if (name.startsWith("txn")) {
        reportMetric(metric, "txn", "name", name, "db", dbName);
      } else {
        reportMetric(metric, name, "db", dbName);
      }
    }

    private void reportQueryMetric(MetaQueryMetric metric) {
      final String name = metric.name();
      if (name.startsWith("orm")) {
        reportMetric(metric, "db.query", "query", name, "db", dbName, "type", "orm");
      } else if (name.startsWith("sql")) {
        reportMetric(metric, "db.query", "query", name, "db", dbName, "type", "orm");
      } else {
        reportMetric(metric, "db.query", "query", name, "db", dbName, "type", "other");
      }
    }

    private void reportMetric(MetaTimedMetric metric, String name, String... tags) {
      reporter.countWithTimestamp(nm(name, ".count"), metric.count(), epochSecs, tags);
      reporter.gaugeWithTimestamp(nm(name, ".max"), metric.max(), epochSecs, tags);
      reporter.gaugeWithTimestamp(nm(name, ".mean"), metric.mean(), epochSecs, tags);
      reporter.gaugeWithTimestamp(nm(name, ".total"), metric.total(), epochSecs, tags);
    }
  }

}
