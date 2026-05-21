package io.avaje.metrics.statsd;

import com.timgroup.statsd.StatsDClient;
import io.ebean.Database;
import io.ebean.datasource.DataSourcePool;
import io.ebean.datasource.PoolStatus;
import io.ebean.meta.MetaCountMetric;
import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.meta.ServerMetrics;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;

import static io.avaje.metrics.statsd.Trim.qry;

final class DatabaseReporter implements StatsdReporter.Reporter {

  private final Database database;
  private final boolean verbose;
  private final String dbTag;
  private final ConcurrentHashMap<String, String> countMetricNames = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, MetricNames> timedMetricNames = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, String> queryTags = new ConcurrentHashMap<>();
  private final MetricNames txnMetricNames = MetricNames.of("ebean.txn");
  private final MetricNames queryMetricNames = MetricNames.of("ebean.query");

  private DatabaseReporter(Database database, boolean verbose) {
    this.database = database;
    this.verbose = verbose;
    this.dbTag = "db:" + database.name();
  }

  static StatsdReporter.Reporter reporter(Database database, boolean verbose) {
    return new DatabaseReporter(database, verbose);
  }

  @Override
  public void report(StatsDClient sender) {
    new DReport(sender).report();
  }

  private final class DReport {

    private final StatsDClient reporter;
    private final ServerMetrics dbMetrics;
    private final long epochSecs;

    private DReport(StatsDClient reporter) {
      this.reporter = reporter;
      this.dbMetrics = database.metaInfo().collectMetrics();
      this.epochSecs = System.currentTimeMillis() / 1000;
      DataSource dataSource = database.dataSource();
      if (dataSource instanceof DataSourcePool) {
        DataSourcePool pool = (DataSourcePool) dataSource;
        poolMetrics(verbose, pool, "type:main");
      }
      DataSource readDatasource = database.readOnlyDataSource();
      if (readDatasource instanceof DataSourcePool && readDatasource != dataSource) {
        DataSourcePool readOnlyPool = (DataSourcePool) readDatasource;
        poolMetrics(verbose, readOnlyPool, "type:readonly");
      }
    }

    private void poolMetrics(boolean verbose, DataSourcePool pool, String tag) {
      if (verbose) {
        poolMetrics(pool, tag);
      } else {
        reporter.gaugeWithTimestamp("datasource.pool.size", pool.size(), epochSecs, dbTag, tag);
      }
    }

    private void poolMetrics(DataSourcePool pool, String tag) {
      PoolStatus status = pool.status(true);
      int size = status.busy() + status.free();
      reporter.gaugeWithTimestamp("datasource.pool.size", size, epochSecs, dbTag, tag);

      long meanAcquireMicros = status.meanAcquireNanos() / 1000;
      reporter.gaugeWithTimestamp("datasource.pool.meanAcquireMicros", meanAcquireMicros, epochSecs, dbTag, tag);

      reporter.gaugeWithTimestamp("datasource.pool.usageCount", status.hitCount(), epochSecs, dbTag, tag);
      reporter.gaugeWithTimestamp("datasource.pool.acquireMicros", status.totalAcquireMicros(), epochSecs, dbTag, tag);
      int waitCount = status.waitCount();
      if (waitCount > 0) {
        reporter.gaugeWithTimestamp("datasource.pool.waitCount", waitCount, epochSecs, dbTag, tag);
      }
      long waitMicros = status.totalWaitMicros();
      if (waitMicros > 0) {
        reporter.gaugeWithTimestamp("datasource.pool.waitMicros", waitMicros, epochSecs, dbTag, tag);
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
      reporter.count(countMetricName(countMetric.name()), countMetric.count(), dbTag);
    }

    private void reportTimedMetric(MetaTimedMetric metric) {
      final String name = metric.name();
      if (name.startsWith("txn.")) {
        reportMetric(metric, txnMetricNames, dbTag, queryTag(name));
      } else {
        reportMetric(metric, timedMetricNamesFor(name), dbTag);
      }
    }

    private void reportQueryMetric(MetaQueryMetric metric) {
      final var name = metric.name();
      final var queryTag = queryTag(name);
      if (name.startsWith("orm")) {
        reportMetric(metric, queryMetricNames, dbTag, "type:orm", queryTag);
      } else if (name.startsWith("sql")) {
        reportMetric(metric, queryMetricNames, dbTag, "type:sql", queryTag);
      } else {
        reportMetric(metric, queryMetricNames, dbTag, "type:other", queryTag);
      }
    }

    private void reportMetric(MetaTimedMetric metric, MetricNames names, String... tags) {
      reporter.countWithTimestamp(names.count(), metric.count(), epochSecs, tags);
      reporter.gaugeWithTimestamp(names.max(), metric.max(), epochSecs, tags);
      reporter.gaugeWithTimestamp(names.mean(), metric.mean(), epochSecs, tags);
      reporter.gaugeWithTimestamp(names.total(), metric.total(), epochSecs, tags);
    }
  }

  private String countMetricName(String name) {
    return countMetricNames.computeIfAbsent(name, value -> "ebean.count." + value);
  }

  private MetricNames timedMetricNamesFor(String name) {
    return timedMetricNames.computeIfAbsent(name, value -> MetricNames.of("ebean.timed." + value));
  }

  private String queryTag(String name) {
    return queryTags.computeIfAbsent(name, value -> "label:" + qry(value));
  }
}
