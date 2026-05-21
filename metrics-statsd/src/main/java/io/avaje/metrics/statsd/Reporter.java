package io.avaje.metrics.statsd;

import com.timgroup.statsd.StatsDClient;
import io.avaje.metrics.*;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;

import static io.avaje.metrics.statsd.Trim.trim;

@NullMarked
final class Reporter implements Runnable, AutoCloseable, StatsdReporter {

  private static final String LABEL_PREFIX = "label:";
  private static final String WEB_API_PREFIX = "web.api.";
  private static final String WEB_API_NAME = "web.api";
  private static final String APP_PREFIX = "app.";
  private static final String APP_COMPONENT_NAME = "app.component";

  private final MetricRegistry registry;
  private final StatsDClient client;
  private final long timedThreshold;
  private final List<StatsdReporter.Reporter> reporters;
  private final ScheduledTask scheduledTask;
  private final AtomicBoolean started = new AtomicBoolean(false);
  private final ConcurrentHashMap<String, MetricNames> metricNames = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Metric.ID, MetricTarget> timerTargets = new ConcurrentHashMap<>();

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
  public StatsdReporter start() {
    scheduledTask.start();
    started.set(true);
    return this;
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

    private void sendValues(Meter.Stats stats, String name, String[] tags) {
      if (stats.count() > 0) {
        var names = metricNamesFor(name);
        client.countWithTimestamp(names.count(), stats.count(), epochSecs, tags);
        client.gaugeWithTimestamp(names.total(), stats.total(), epochSecs, tags);
        client.gaugeWithTimestamp(names.mean(), stats.mean(), epochSecs, tags);
        client.gaugeWithTimestamp(names.max(), stats.max(), epochSecs, tags);
      }
    }

    @Override
    public void visit(Timer.Stats timed) {
      if (timedThreshold != 0 && timedThreshold >= timed.total()) {
        return;
      }
      var target = timerTargetFor(timed.id());
      sendValues(timed, target.name(), target.tags());
    }

    @Override
    public void visit(Meter.Stats stats) {
      sendValues(stats, stats.name(), stats.tags());
    }

    @Override
    public void visit(Counter.Stats counter) {
      client.countWithTimestamp(counter.name(), counter.count(), epochSecs, counter.tags());
    }

    @Override
    public void visit(GaugeDouble.Stats gauge) {
      client.gaugeWithTimestamp(gauge.name(), gauge.value(), epochSecs, gauge.tags());
    }

    @Override
    public void visit(GaugeLong.Stats gauge) {
      client.gaugeWithTimestamp(gauge.name(), gauge.value(), epochSecs, gauge.tags());
    }
  }

  private MetricNames metricNamesFor(String name) {
    return metricNames.computeIfAbsent(name, MetricNames::of);
  }

  private MetricTarget timerTargetFor(Metric.ID id) {
    return timerTargets.computeIfAbsent(id, this::createTimerTarget);
  }

  private MetricTarget createTimerTarget(Metric.ID id) {
    var name = id.name();
    var tags = id.tags();
    var rawTags = tags.array();
    if (hasLabelTag(rawTags)) {
      return new MetricTarget(name, rawTags);
    }
    if (name.startsWith(WEB_API_PREFIX)) {
      return new MetricTarget(WEB_API_NAME, tags.append(LABEL_PREFIX + trim(name, WEB_API_PREFIX.length())));
    }
    if (name.startsWith(APP_PREFIX)) {
      return new MetricTarget(APP_COMPONENT_NAME, tags.append(LABEL_PREFIX + trim(name, APP_PREFIX.length())));
    }
    return new MetricTarget(name, rawTags);
  }

  private static boolean hasLabelTag(String[] tags) {
    for (String tag : tags) {
      if (tag.startsWith(LABEL_PREFIX)) {
        return true;
      }
    }
    return false;
  }

  private static final class MetricTarget {

    private final String name;
    private final String[] tags;

    private MetricTarget(String name, String[] tags) {
      this.name = name;
      this.tags = tags;
    }

    private String name() {
      return name;
    }

    private String[] tags() {
      return tags;
    }
  }
}
