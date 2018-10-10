package org.avaje.metric.core;

import org.avaje.metric.MetricName;
import org.avaje.metric.TimedMetric;

import java.util.regex.Pattern;


/**
 * Provides a name for metrics.
 * <p>
 * Typically names are based on a class and method name.
 * </p>
 */
class DefaultMetricName implements MetricName {

  private static final String ERROR = ".error";

  private final String group;
  private final String type;
  private final String name;
  private final String simpleName;

  /** Compiled Regex for finding replacing symbols in metric names. */
  private static final Pattern REPLACE_METRIC_NAME = Pattern.compile("\\$$");

  /**
   * Parse and return a MetricName based on dot notation.
   * <p>
   * This is expecting "package.type.name" format.
   * </p>
   */
  static DefaultMetricName parse(String rawName) {

    int lastDot;
    if (rawName.endsWith(ERROR)) {
      lastDot = rawName.lastIndexOf('.', rawName.length() - 7);

    } else {
      lastDot = rawName.lastIndexOf('.');
    }

    if (lastDot < 1) {
      return new DefaultMetricName(rawName, null, null);
    }
    int secLastDot = rawName.lastIndexOf('.', lastDot-1);
    if (secLastDot > 0) {
      String name = rawName.substring(lastDot+1);
      String type = rawName.substring(secLastDot+1, lastDot);
      String group = rawName.substring(0, secLastDot);
      return new DefaultMetricName(group, type, name);
    }
    String name = rawName.substring(lastDot+1);
    String type = rawName.substring(0, lastDot);
    return new DefaultMetricName(type, null, name);
  }

  /**
   * Creates a new {@link DefaultMetricName} without a scope.
   *
   * @param klass
   *          the {@link Class} to which the {@link TimedMetric} belongs
   * @param name
   *          the name of the {@link TimedMetric}
   */
  DefaultMetricName(Class<?> klass, String name) {

    this(klass.getPackage() == null ? "" : klass.getPackage().getName(), REPLACE_METRIC_NAME.matcher(klass.getSimpleName()).replaceAll(""), name);
  }

  /**
   * Creates a new MetricName.
   */
  DefaultMetricName(String group, String type, String name) {
    this.group = group;
    this.type = type;
    this.name = name;
    this.simpleName = createSimpleName(group, type, name);
  }

  /**
   * Create a MetricName with the nameSuffix appended to the original name.
   */
  @Override
  public MetricName withSuffix(String nameSuffix) {
    return new DefaultMetricName(group, type, name + nameSuffix);
  }

  /**
   * Create a similar MetricName changing just the name.
   * <p>
   * Typically used via MetricNameCache.
   * </p>
   */
  @Override
  public MetricName withName(String newName) {
    return new DefaultMetricName(group, type, newName);
  }

  /**
   * Returns the group to which the {@link TimedMetric} belongs. For class-based
   * metrics, this will be the package name of the {@link Class} to which the
   * {@link TimedMetric} belongs.
   *
   * @return the group to which the {@link TimedMetric} belongs
   */
  @Override
  public String getGroup() {
    return group;
  }

  /**
   * Returns the type to which the {@link TimedMetric} belongs. For class-based
   * metrics, this will be the simple class name of the {@link Class} to which
   * the {@link TimedMetric} belongs.
   *
   * @return the type to which the {@link TimedMetric} belongs
   */
  @Override
  public String getType() {
    return type;
  }

  /**
   * Returns the name of the metric.
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Return a simple java like name.
   */
  @Override
  public String getSimpleName() {
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
    final DefaultMetricName that = (DefaultMetricName) o;
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
    return simpleName.compareTo(o.getSimpleName());
  }

  private static String createSimpleName(String group, String type, String name) {
    StringBuilder sb = new StringBuilder(80);
    if (group != null) {
      sb.append(group);
    }
    if (type != null) {
      sb.append('.').append(type);
    }
    if (name != null && name.length() > 0) {
      sb.append('.').append(name);
    }
    return sb.toString().replace(' ', '-');
  }
}
