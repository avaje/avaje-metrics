package io.avaje.metrics.otel;

import io.avaje.applog.AppLog;
import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.ScheduledTask;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

final class DOtelReporter implements OtelReporter, Runnable {

  private static final System.Logger log = AppLog.getLogger(OtelReporter.class);

  private final MetricRegistry registry;
  private final OtelVisitor visitor;
  private final ScheduledTask scheduledTask;
  private final AtomicBoolean started = new AtomicBoolean(false);

  DOtelReporter(MetricRegistry registry, OtelVisitor visitor, int schedule, TimeUnit scheduleTimeUnit) {
    this.registry = registry;
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
  public void run() {
    long start = System.currentTimeMillis();
    try {
      List<Metric.Statistics> metrics = registry.collectMetrics();
      for (Metric.Statistics metric : metrics) {
        metric.visit(visitor);
      }
      log.log(DEBUG, "reported {0} metrics to OpenTelemetry in {1}ms", metrics.size(), System.currentTimeMillis() - start);
    } catch (Exception e) {
      log.log(WARNING, "Error reporting metrics to OpenTelemetry", e);
    }
  }
}
