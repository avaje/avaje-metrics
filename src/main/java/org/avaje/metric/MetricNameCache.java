package org.avaje.metric;

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
public final class MetricNameCache {

  private final MetricName baseName;

  private final ConcurrentHashMap<String, MetricName> cache = new ConcurrentHashMap<String, MetricName>();

  /**
   * Create basing the name off the Class.
   */
  public MetricNameCache(Class<?> klass) {
    this(new MetricName(klass, "", null));
  }

  /**
   * Create providing a base MetricName.
   */
  public MetricNameCache(MetricName baseName) {
    this.baseName = baseName;
  }
  
  /**
   * Return the MetricName from the cache creating it if required.
   * <p>
   * Typically the name passed in could be a soap operation name or method name.
   * </p>
   */
  public MetricName get(String name) {

    MetricName metricName = cache.get(name);
    if (metricName == null) {
      metricName = baseName.deriveWithName(name);
      MetricName oldMetricName = cache.putIfAbsent(name, metricName);
      if (oldMetricName != null) {
        return oldMetricName;
      }
    }
    return metricName;
  }

}
