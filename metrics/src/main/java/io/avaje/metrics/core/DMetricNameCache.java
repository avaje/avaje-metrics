package io.avaje.metrics.core;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides a cache of MetricNames with a common base name derived from a class
 * without a scope.
 * <p>
 * The MetricNames cached here typically represent different methods on the same
 * class. This is used to relatively efficiently dynamically define a metric
 * name (for example, on soap operation name or method name).
 * </p>
 */
final class DMetricNameCache {

  private final String baseName;
  private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

  /**
   * Create basing the name off the Class.
   */
  DMetricNameCache(Class<?> klass) {
    this(klass.getName());
  }

  /**
   * Create providing a base MetricName.
   */
  DMetricNameCache(String baseName) {
    this.baseName = baseName;
  }

  /**
   * Return the MetricName from the cache creating it if required.
   */
  String get(String name) {
    String metricName = cache.get(name);
    if (metricName == null) {
      metricName = deriveWithName(name);
      String oldMetricName = cache.putIfAbsent(name, metricName);
      if (oldMetricName != null) {
        return oldMetricName;
      }
    }
    return metricName;
  }

  /**
   * Create a similar MetricName changing just the name.
   * <p>
   * Typically used via MetricNameCache.
   * </p>
   */
  private String deriveWithName(String newName) {
    return baseName + "." + newName;
  }
}
