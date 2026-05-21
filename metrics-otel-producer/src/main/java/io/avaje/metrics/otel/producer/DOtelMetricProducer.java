package io.avaje.metrics.otel.producer;

import io.avaje.metrics.MetricRegistry;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;

import java.util.Collection;
import java.util.function.LongSupplier;

import static java.util.Objects.requireNonNull;

final class DOtelMetricProducer implements OtelMetricProducer {

  private final MetricRegistry registry;
  private final MetricDataMapper mapper;
  private final LongSupplier epochNanosSource;
  private long lastCollectionEpochNanos;

  DOtelMetricProducer(
    MetricRegistry registry,
    InstrumentationScopeInfo scopeInfo,
    long timedThresholdMicros,
    LongSupplier epochNanosSource) {

    this.registry = requireNonNull(registry, "registry");
    this.mapper = new MetricDataMapper(requireNonNull(scopeInfo, "scopeInfo"), timedThresholdMicros);
    this.epochNanosSource = requireNonNull(epochNanosSource, "epochNanosSource");
    this.lastCollectionEpochNanos = epochNanosSource.getAsLong();
  }

  @Override
  public synchronized Collection<MetricData> produce(Resource resource) {
    requireNonNull(resource, "resource");
    var startEpochNanos = lastCollectionEpochNanos;
    var epochNanos = epochNanosSource.getAsLong();
    var statistics = registry.collectMetrics();
    lastCollectionEpochNanos = epochNanos;
    return mapper.map(resource, startEpochNanos, epochNanos, statistics);
  }
}
