package io.avaje.metrics.statsd;

import com.timgroup.statsd.StatsDClient;
import io.avaje.metrics.*;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@NullMarked
final class Reporter implements Runnable, AutoCloseable, StatsdReporter {

  private final MetricRegistry registry;
  private final StatsDClient client;
  private final long timedThreshold;
  private final List<StatsdReporter.Reporter> reporters;
  private final ScheduledTask scheduledTask;
  private final AtomicBoolean started = new AtomicBoolean(false);

  Reporter(MetricRegistry registry, StatsDClient client, long timedThreshold, int schedule,
           TimeUnit scheduleTimeUnit, List<StatsdReporter.Reporter> reporters) {
    this.registry = registry;
    this.client = client;
    this.timedThreshold = timedThreshold;
    this.reporters = reporters;
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
  public void run() {
    var visitor = new AvajeMetricsVisitor();
    for (Metric.Statistics metric : registry.collectMetrics()) {
      metric.visit(visitor);
    }
    for (StatsdReporter.Reporter reporter : reporters) {
      reporter.report(client);
    }
  }

  private final class AvajeMetricsVisitor implements Metric.Visitor {

    private final long epochSecs = System.currentTimeMillis() / 1000;

    private void sendValues(Meter.Stats stats, String name, String... tags) {
      if (stats.count() > 0) {
        client.countWithTimestamp(name + ".count", stats.count(), epochSecs, tags);
        client.gaugeWithTimestamp(name + ".total", stats.total(), epochSecs, tags);
        client.gaugeWithTimestamp(name + ".mean", stats.mean(), epochSecs, tags);
        client.gaugeWithTimestamp(name + ".max", stats.max(), epochSecs, tags);
      }
    }

    @Override
    public void visit(Timer.Stats timed) {
      if (timedThreshold == 0 || timedThreshold < timed.total()) {
        if (timed.name().startsWith("web.api")) {
          sendValues(timed, "web.api", "name", timed.name());
        } else if (timed.name().startsWith("app.")) {
          sendValues(timed, "app.method", "name", timed.name());
        } else {
          sendValues(timed, timed.name());
        }
      }
    }

    @Override
    public void visit(Meter.Stats stats) {
      sendValues(stats, stats.name());
    }

    @Override
    public void visit(Counter.Stats counter) {
      client.countWithTimestamp(counter.name(), counter.count(), epochSecs);
    }

    @Override
    public void visit(GaugeDouble.Stats gauge) {
      client.gaugeWithTimestamp(gauge.name(), gauge.value(), epochSecs);
    }

    @Override
    public void visit(GaugeLong.Stats gauge) {
      client.gaugeWithTimestamp(gauge.name(), gauge.value(), epochSecs);
    }
  }
}
