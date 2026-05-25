package io.avaje.metrics.prometheus;

import io.avaje.metrics.CollectionMode;
import io.avaje.metrics.MetricRegistry;

import static java.util.Objects.requireNonNull;

final class DPrometheusMetrics implements PrometheusMetrics {

  private final MetricRegistry registry;
  private final long timedThresholdMicros;
  private final boolean includeMax;
  private final PrometheusNameCache nameCache = new PrometheusNameCache();

  DPrometheusMetrics(MetricRegistry registry, long timedThresholdMicros, boolean includeMax) {
    this.registry = requireNonNull(registry, "registry");
    this.timedThresholdMicros = timedThresholdMicros;
    this.includeMax = includeMax;
  }

  @Override
  public synchronized String scrape() {
    var builder = new StringBuilder(1024);
    write(builder);
    return builder.toString();
  }

  @Override
  public synchronized void write(Appendable appendable) {
    requireNonNull(appendable, "appendable");
    var statistics = registry.collectMetrics(CollectionMode.CUMULATIVE);
    new PrometheusWriter(appendable, timedThresholdMicros, includeMax, nameCache).write(statistics);
  }
}
