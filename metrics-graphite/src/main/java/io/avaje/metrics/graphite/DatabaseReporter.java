package io.avaje.metrics.graphite;

import io.ebean.Database;
import io.ebean.meta.MetaCountMetric;
import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.meta.ServerMetrics;

import java.io.IOException;

/**
 * Ebean Database reporter.
 */
final class DatabaseReporter {

  static final class DReporter implements GraphiteSender.Reporter {

    private final Database database;

    DReporter(Database database) {
      this.database = database;
    }

    @Override
    public void report(GraphiteSender sender) throws IOException {
      new DatabaseReporter(database, sender).report();
    }
  }

  static DReporter reporter(Database database) {
    return new DReporter(database);
  }

  private final GraphiteSender reporter;
  private final ServerMetrics dbMetrics;
  private final long epochSecs;
  private final String dbPrefix;

  private DatabaseReporter(Database database, GraphiteSender reporter) {
    this.reporter = reporter;
    this.dbPrefix = database.name() + ".";
    this.dbMetrics = database.metaInfo().collectMetrics();
    this.epochSecs = System.currentTimeMillis() / 1000;
  }

  void report() throws IOException {
    for (MetaTimedMetric timedMetric : dbMetrics.timedMetrics()) {
      reportMetric(timedMetric);
    }
    for (MetaQueryMetric queryMetric : dbMetrics.queryMetrics()) {
      reportQueryMetric(queryMetric);
    }
    for (MetaCountMetric countMetric : dbMetrics.countMetrics()) {
      reportCountMetric(countMetric);
    }
  }

  private void reportCountMetric(MetaCountMetric countMetric) throws IOException {
    write(countMetric.count(), countMetric.name(), ".count");
  }

  private void reportQueryMetric(MetaQueryMetric metric) throws IOException {
    String name = metric.name();
    if (name != null) {
      write(metric.count(), name, ".count");
      write(metric.max(), name, ".max");
      write(metric.mean(), name, ".mean");
      write(metric.total(), name, ".total");
    }
  }

  private void reportMetric(MetaTimedMetric timedMetric) throws IOException {
    String name = timedMetric.name();
    write(timedMetric.count(), name, ".count");
    write(timedMetric.max(), name, ".max");
    write(timedMetric.mean(), name, ".mean");
    write(timedMetric.total(), name, ".total");
  }

  private void write(long value, String name, String metric) throws IOException {
    reporter.send(String.valueOf(value), epochSecs, dbPrefix, name, metric);
  }
}
