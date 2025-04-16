package io.avaje.metrics.statsd;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClientBuilder;
import com.timgroup.statsd.StatsDClient;
import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.Metrics;
import io.ebean.Database;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

final class StatsdBuilder implements StatsdReporter.Builder {

  private final List<StatsdReporter.Reporter> reporters = new ArrayList<>();
  private MetricRegistry registry;
  private String hostname = "localhost";
  private int port = NonBlockingStatsDClient.DEFAULT_DOGSTATSD_PORT;
  private StatsDClient client;
  private long timedThresholdMicros;
  private String[] tags;
  private int schedule = 60;
  private TimeUnit scheduleTimeUnit = TimeUnit.SECONDS;

  @Override
  public StatsdReporter.Builder hostname(String hostname) {
    this.hostname = requireNonNull(hostname);
    return this;
  }

  @Override
  public StatsdReporter.Builder port(int port) {
    this.port = port;
    return this;
  }

  @Override
  public StatsdReporter.Builder client(StatsDClient client) {
    this.client = client;
    return this;
  }

  @Override
  public StatsdReporter.Builder timedThresholdMicros(long timedThresholdMicros) {
    this.timedThresholdMicros = timedThresholdMicros;
    return this;
  }

  @Override
  public StatsdReporter.Builder tags(String[] tags) {
    this.tags = tags;
    return this;
  }

  @Override
  public StatsdReporter.Builder schedule(int schedule, TimeUnit timeUnit) {
    this.schedule = schedule;
    this.scheduleTimeUnit = requireNonNull(timeUnit);
    return this;
  }

  @Override
  public StatsdReporter.Builder registry(MetricRegistry registry) {
    this.registry = requireNonNull(registry);
    return this;
  }

  @Override
  public StatsdReporter.Builder database(Database database) {
    reporters.add(DatabaseReporter.reporter(database));
    return this;
  }

  @Override
  public StatsdReporter.Builder reporter(StatsdReporter.Reporter reporter) {
    reporters.add(requireNonNull(reporter));
    return this;
  }

  @Override
  public Reporter build() {
    if (registry == null) {
      registry = Metrics.registry();
    }
    if (client == null) {
      client = new NonBlockingStatsDClientBuilder()
        .hostname(requireNonNull(hostname))
        .port(port)
        .constantTags(tags)
        .build();
    }

    return new Reporter(registry, client, timedThresholdMicros, schedule, scheduleTimeUnit, reporters);
  }
}
