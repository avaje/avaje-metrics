package io.avaje.metrics.otel.producer;

import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.Metrics;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;

import static java.util.Objects.requireNonNull;

final class DOtelMetricProducerBuilder implements OtelMetricProducer.Builder {

  private static final String DEFAULT_SCOPE = "io.avaje.metrics";

  private MetricRegistry registry;
  private long timedThresholdMicros;
  private String scopeName = DEFAULT_SCOPE;

  @Override
  public OtelMetricProducer.Builder registry(MetricRegistry registry) {
    this.registry = requireNonNull(registry, "registry");
    return this;
  }

  @Override
  public OtelMetricProducer.Builder timedThresholdMicros(long threshold) {
    this.timedThresholdMicros = threshold;
    return this;
  }

  @Override
  public OtelMetricProducer.Builder scopeName(String scopeName) {
    this.scopeName = requireNonNull(scopeName, "scopeName");
    return this;
  }

  @Override
  public OtelMetricProducer build() {
    var effectiveRegistry = registry != null ? registry : Metrics.registry();
    var scopeInfo = InstrumentationScopeInfo.create(scopeName);
    return new DOtelMetricProducer(
      effectiveRegistry,
      scopeInfo,
      timedThresholdMicros,
      DOtelMetricProducerBuilder::systemEpochNanos);
  }

  private static long systemEpochNanos() {
    return System.currentTimeMillis() * 1_000_000L;
  }
}
