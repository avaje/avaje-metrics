package io.avaje.metrics;

/**
 * Cache of MetricNames that share a common base name.
 * <p>
 * Typically this is used when the full name of the metric is known at runtime and must be looked
 * up. Using this cache avoids extra parsing of the metrics name and this MetricNameCache exists for
 * that performance reason.
 */
public interface MetricNameCache {

  /**
   * Return the MetricName from the cache creating it if required.
   * <p>
   * Typically the name passed in could be a soap operation name or method name.
   * </p>
   */
  MetricName get(String name);

}