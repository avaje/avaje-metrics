package org.avaje.metric.core;

import org.avaje.metric.MetricName;
import org.avaje.metric.MetricNameCache;

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
final class DefaultMetricNameCache implements MetricNameCache {

  private final MetricName baseName;

  private final ConcurrentHashMap<String, MetricName> cache = new ConcurrentHashMap<>();

  /**
   * Create basing the name off the Class.
   */
  DefaultMetricNameCache(Class<?> klass) {
    this(new DefaultMetricName(klass, ""));
  }

  /**
   * Create providing a base MetricName.
   */
  DefaultMetricNameCache(MetricName baseName) {
    this.baseName = baseName;
  }

  /**
   * Return the MetricName from the cache creating it if required.
   * <p>
   * Typically the name passed in could be a soap operation name or method name.
   * </p>
   */
  @Override
  public MetricName get(String name) {

    MetricName metricName = cache.get(name);
    if (metricName == null) {
      metricName = deriveWithName(name);
      MetricName oldMetricName = cache.putIfAbsent(name, metricName);
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
  private MetricName deriveWithName(String newName) {
    return baseName.append(newName);
  }
}
