package io.avaje.metrics.otel.reporter;

import io.avaje.applog.AppLog;
import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricsProvider;
import io.avaje.metrics.ScheduledTask;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;
import static java.util.Objects.requireNonNull;

final class DOtelReporter implements OtelReporter, Runnable {

  private static final System.Logger log = AppLog.getLogger(OtelReporter.class);

  private final MetricsProvider metricsProvider;
  private final OtelVisitor visitor;
  private final ScheduledTask scheduledTask;
  private final AtomicBoolean started = new AtomicBoolean(false);

  DOtelReporter(MetricsProvider metricsProvider, OtelVisitor visitor, int schedule, TimeUnit scheduleTimeUnit) {
    this.metricsProvider = requireNonNull(metricsProvider, "metricsProvider");
    this.visitor = visitor;
    this.scheduledTask = ScheduledTask.builder()
        .schedule(schedule, schedule, scheduleTimeUnit)
        .task(this)
        .build();
  }

  @Override
  public void start() {
    scheduledTask.start();
    started.set(true);
  }

  @Override
  public void close() {
    if (started.get()) {
      scheduledTask.cancel(true);
    }
  }

  @Override
  public void report() {
    run();
  }

  @Override
  public void report(List<Metric.Statistics> snapshot) {
    long start = System.currentTimeMillis();
    try {
      reportSnapshot(requireNonNull(snapshot, "snapshot"));
      log.log(DEBUG, "reported {0} metrics to OpenTelemetry in {1}ms", snapshot.size(), System.currentTimeMillis() - start);
    } catch (Exception e) {
      log.log(WARNING, "Error reporting metrics to OpenTelemetry", e);
    }
  }

  @Override
  public void run() {
    try {
      report(metricsProvider.provide());
    } catch (Exception e) {
      log.log(WARNING, "Error obtaining metrics for OpenTelemetry", e);
    }
  }

  private void reportSnapshot(List<Metric.Statistics> snapshot) {
    for (Metric.Statistics metric : snapshot) {
      metric.visit(visitor);
    }
  }
}
