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
public class DefaultMetricName implements MetricName {

  /**
   * Group used when it is undefined in {@link #parse(String)}.
   */
  public static final String UNDEFINED_GROUP = "o";
  
  private final String group;
  private final String type;
  private final String name;
  private final String simpleName;

  /** Compiled Regex for finding replacing symbols in metric names. */
  private static final Pattern REPLACE_METRIC_NAME = Pattern.compile("\\$$");

  /**
   * Create a base name with a group and type but no name.  
   */
  public static DefaultMetricName createBaseName(String group, String type) {
    return new DefaultMetricName(group, type, null);
  }
  
  /**
   * Parse and return a MetricName based on dot notation.
   * <p>
   * This is expecting "package.type.name" format.
   * </p>
   */
  public static DefaultMetricName parse(String rawName) {
    int lastDot = rawName.lastIndexOf('.');
    if (lastDot < 1) {
      return new DefaultMetricName(null, null, rawName);
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
    return new DefaultMetricName(UNDEFINED_GROUP, type, name);
  }
  
  /**
   * Creates a new {@link DefaultMetricName} without a scope.
   * 
   * @param klass
   *          the {@link Class} to which the {@link TimedMetric} belongs
   * @param name
   *          the name of the {@link TimedMetric}
   */
  public DefaultMetricName(Class<?> klass, String name) {

    this(klass.getPackage() == null ? "" : klass.getPackage().getName(), REPLACE_METRIC_NAME.matcher(klass.getSimpleName()).replaceAll(""), name);
  }

  /**
   * Creates a new MetricName.
   */
  public DefaultMetricName(String group, String type, String name) {
    if (group == null) {
      throw new IllegalArgumentException("group needs to be specified");
    }
    if (type == null) {
      throw new IllegalArgumentException("type needs to be specified for JMX bean name support");
    }
    this.group = group;
    this.type = type;
    this.name = name;
    this.simpleName = createSimpleName(group, type, name);
  }

  /**
   * Create a MetricName with the nameSuffix appended to the original name.
   */
  public MetricName withSuffix(String nameSuffix) {
    return new DefaultMetricName(group, type, name + nameSuffix);
  }

  
  /**
   * Create a similar MetricName changing just the name.
   * <p>
   * Typically used via MetricNameCache.
   * </p>
   */
  public DefaultMetricName withName(String newName) {
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

  public int compareTo(MetricName o) {
    return simpleName.compareTo(o.getSimpleName());
  }

  private static String createSimpleName(String group, String type, String name) {
    StringBuilder sb = new StringBuilder(80);
    sb.append(group);
    if (type != null) {
      sb.append('.').append(type);
    }
    if (name != null && name.length() > 0) {
      sb.append('.').append(name);
    }
    return sb.toString().replace(' ', '-');
  }
}
