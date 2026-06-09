package io.avaje.metrics.graphite;

import io.avaje.applog.AppLog;
import io.ebean.Database;
import io.ebean.meta.MetaCountMetric;
import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.meta.ServerMetrics;

import java.io.IOException;
import java.util.function.Consumer;

import static java.lang.System.Logger.Level.WARNING;

/**
 * Ebean Database reporter.
 */
final class DatabaseReporter {

  private static final System.Logger log = AppLog.getLogger(GraphiteReporter.class);

  static final class DReporter implements GraphiteSender.Reporter {

    private final Database database;
    private final Consumer<ServerMetrics> forwardTo;

    DReporter(Database database, Consumer<ServerMetrics> forwardTo) {
      this.database = database;
      this.forwardTo = forwardTo;
    }

    @Override
    public void report(GraphiteSender sender) throws IOException {
      new DatabaseReporter(database, sender, forwardTo).report();
    }
  }

  static DReporter reporter(Database database) {
    return new DReporter(database, null);
  }

  static DReporter reporter(Database database, Consumer<ServerMetrics> forwardTo) {
    return new DReporter(database, forwardTo);
  }

  private final GraphiteSender reporter;
  private final ServerMetrics dbMetrics;
  private final long epochSecs;
  private final String dbPrefix;
  private final Consumer<ServerMetrics> forwardTo;

  private DatabaseReporter(Database database, GraphiteSender reporter, Consumer<ServerMetrics> forwardTo) {
    this.reporter = reporter;
    this.dbPrefix = database.name() + ".";
    this.dbMetrics = database.metaInfo().collectMetrics();
    this.epochSecs = System.currentTimeMillis() / 1000;
    this.forwardTo = forwardTo;
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
    if (forwardTo != null) {
      try {
        forwardTo.accept(dbMetrics);
      } catch (Throwable e) {
        log.log(WARNING, "forwardTo consumer threw", e);
      }
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
