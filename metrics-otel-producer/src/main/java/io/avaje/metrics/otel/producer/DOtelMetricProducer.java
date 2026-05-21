package io.avaje.metrics.otel.producer;

import io.avaje.metrics.MetricRegistry;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;

import java.time.Clock;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

final class DOtelMetricProducer implements OtelMetricProducer {

  private final MetricRegistry registry;
  private final MetricDataMapper mapper;
  private final Clock clock;
  private long lastCollectionEpochNanos;

  DOtelMetricProducer(
    MetricRegistry registry,
    InstrumentationScopeInfo scopeInfo,
    long timedThresholdMicros,
    Clock clock) {

    this.registry = requireNonNull(registry, "registry");
    this.mapper = new MetricDataMapper(requireNonNull(scopeInfo, "scopeInfo"), timedThresholdMicros);
    this.clock = requireNonNull(clock, "clock");
    this.lastCollectionEpochNanos = epochNanos(clock);
  }

  @Override
  public synchronized Collection<MetricData> produce(Resource resource) {
    requireNonNull(resource, "resource");
    var startEpochNanos = lastCollectionEpochNanos;
    var epochNanos = epochNanos(clock);
    var statistics = registry.collectMetrics();
    lastCollectionEpochNanos = epochNanos;
    return mapper.map(resource, startEpochNanos, epochNanos, statistics);
  }

  private static long epochNanos(Clock clock) {
    var instant = clock.instant();
    return instant.getEpochSecond() * 1_000_000_000L + instant.getNano();
  }
}
