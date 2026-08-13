package io.avaje.metrics;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Provides metric statistics for a reporting destination.
 *
 * <p>The provider controls which metrics are collected and returned. This allows
 * reporting integrations to use a filtered or otherwise composed view of a
 * {@link MetricRegistry} rather than always collecting directly from it.
 */
@FunctionalInterface
public interface MetricsProvider {

  /**
   * Provide metric statistics using the given collection mode.
   */
  List<Metric.Statistics> provide(CollectionMode mode);

  /**
   * Provide delta metric statistics.
   */
  default List<Metric.Statistics> provide() {
    return provide(CollectionMode.DELTA);
  }

  /**
   * Create a provider that collects directly from the given registry.
   */
  static MetricsProvider forRegistry(MetricRegistry registry) {
    requireNonNull(registry, "registry");
    return registry::collectMetrics;
  }
}
