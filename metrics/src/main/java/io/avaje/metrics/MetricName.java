package io.avaje.metrics;

/**
 * The name of the metric.
 */
public interface MetricName extends Comparable<io.avaje.metrics.MetricName> {

  /**
   * Create a Metric name by parsing a name that is expected to include periods (dot notation
   * similar to package.Class.method).
   */
  static MetricName of(String name) {
    return MetricManager.name(name);
  }

  /**
   * Create a MetricName based on a class and name.
   * <p>
   * Often the name maps to a method name.
   */
  static MetricName of(Class<?> cls, String name) {
    return MetricManager.name(cls, name);
  }

  /**
   * Return a simple java like name.
   */
  String getSimpleName();

  /**
   * Create and return another MetricName by appending the suffix.
   */
  MetricName append(String suffix);

  /**
   * Return true if the metric name starts with the given prefix.
   */
  boolean startsWith(String prefix);

  /**
   * Return true if the metric is considered an "error" metric with a name ending in ".error".
   */
  boolean isError();
}
