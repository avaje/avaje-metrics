package io.avaje.metrics;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Coordinates a metrics provider that is installed after reporter creation.
 *
 * <p>This is useful when a provider depends on a bean that is initialized after
 * the metrics reporters, such as an Ebean database.
 */
public final class MetricsProviderCoordinator implements MetricsProvider {

  private volatile MetricsProvider delegate;

  /**
   * Provides metrics using the installed provider.
   *
   * @throws IllegalStateException if a provider has not been installed
   */
  @Override
  public List<Metric.Statistics> provide(CollectionMode mode) {
    var current = delegate;
    if (current == null) {
      throw new IllegalStateException("Metrics provider has not been installed");
    }
    return current.provide(mode);
  }

  /**
   * Installs the provider used for subsequent collection.
   *
   * @throws IllegalStateException if a provider has already been installed
   * @throws NullPointerException if provider is null
   */
  public synchronized void install(MetricsProvider provider) {
    if (delegate != null) {
      throw new IllegalStateException("Metrics provider has already been installed");
    }
    delegate = requireNonNull(provider, "provider");
  }
}
