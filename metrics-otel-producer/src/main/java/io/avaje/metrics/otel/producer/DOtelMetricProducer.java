package io.avaje.metrics.otel.producer;

import io.avaje.metrics.CollectionMode;
import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.MetricsProvider;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;

import java.util.Collection;
import java.util.function.LongSupplier;

import static java.util.Objects.requireNonNull;

final class DOtelMetricProducer implements OtelMetricProducer {

  private final MetricsProvider metricsProvider;
  private final MetricDataMapper mapper;
  private final LongSupplier epochNanosSource;
  private final long startEpochNanos;

  DOtelMetricProducer(
    MetricRegistry registry,
    InstrumentationScopeInfo scopeInfo,
    long timedThresholdMicros,
    LongSupplier epochNanosSource) {

    this(MetricsProvider.forRegistry(registry), scopeInfo, timedThresholdMicros, epochNanosSource);
  }

  DOtelMetricProducer(
    MetricsProvider metricsProvider,
    InstrumentationScopeInfo scopeInfo,
    long timedThresholdMicros,
    LongSupplier epochNanosSource) {

    this.metricsProvider = requireNonNull(metricsProvider, "metricsProvider");
    this.mapper = new MetricDataMapper(requireNonNull(scopeInfo, "scopeInfo"), timedThresholdMicros);
    this.epochNanosSource = requireNonNull(epochNanosSource, "epochNanosSource");
    this.startEpochNanos = epochNanosSource.getAsLong();
  }

  @Override
  public synchronized Collection<MetricData> produce(Resource resource) {
    requireNonNull(resource, "resource");
    var epochNanos = epochNanosSource.getAsLong();
    var statistics = metricsProvider.provide(CollectionMode.CUMULATIVE);
    return mapper.map(resource, startEpochNanos, epochNanos, statistics);
  }
}
