package io.avaje.metrics.prometheus;

import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.Metrics;

import static java.util.Objects.requireNonNull;

final class DPrometheusMetricsBuilder implements PrometheusMetrics.Builder {

  private MetricRegistry registry;
  private long timedThresholdMicros;
  private boolean includeMax = false;

  @Override
  public PrometheusMetrics.Builder registry(MetricRegistry registry) {
    this.registry = requireNonNull(registry, "registry");
    return this;
  }

  @Override
  public PrometheusMetrics.Builder timedThresholdMicros(long threshold) {
    this.timedThresholdMicros = threshold;
    return this;
  }

  @Override
  public PrometheusMetrics.Builder includeMax(boolean includeMax) {
    this.includeMax = includeMax;
    return this;
  }

  @Override
  public PrometheusMetrics build() {
    var effectiveRegistry = registry != null ? registry : Metrics.registry();
    return new DPrometheusMetrics(effectiveRegistry, timedThresholdMicros, includeMax);
  }
}
