package io.avaje.metrics.core;

import io.avaje.metrics.MetricName;

import java.util.regex.Pattern;


/**
 * Provides a name for metrics.
 * <p>
 * Typically names are based on a class and method name.
 * </p>
 */
final class DMetricName implements MetricName {

  private static final String ERROR = ".error";

  /**
   * Compiled Regex for finding replacing symbols in metric names.
   */
  private static final Pattern REPLACE_METRIC_NAME = Pattern.compile("\\$$");

  private final String simpleName;

  /**
   * Creates a new MetricNamegiven the class and method/name.
   */
  DMetricName(Class<?> klass, String name) {
    this(klassName(klass, name));
  }

  private static String klassName(Class<?> klass, String name) {
    String typeName = REPLACE_METRIC_NAME.matcher(klass.getSimpleName()).replaceAll("");
    String metricName = klass.getPackage() == null ? typeName : klass.getPackage().getName() + "." + typeName;
    if (name != null && !name.isEmpty()) {
      metricName += "." + name;
    }
    return metricName;
  }

  /**
   * Creates a new MetricName.
   */
  DMetricName(String simpleName) {
    this.simpleName = simpleName;
  }

  /**
   * Create a MetricName with the nameSuffix appended to the original name.
   */
  @Override
  public MetricName append(String suffix) {
    return new DMetricName(simpleName + "." + suffix);
  }

  /**
   * Return a simple java like name.
   */
  @Override
  public String simpleName() {
    return simpleName;
  }

  @Override
  public boolean startsWith(String prefix) {
    return simpleName.startsWith(prefix);
  }

  @Override
  public boolean isError() {
    return simpleName.endsWith(ERROR);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final DMetricName that = (DMetricName) o;
    return simpleName.equals(that.simpleName);
  }

  @Override
  public int hashCode() {
    return simpleName.hashCode();
  }

  @Override
  public String toString() {
    return simpleName;
  }

  @Override
  public int compareTo(MetricName o) {
    return simpleName.compareTo(o.simpleName());
  }

}
