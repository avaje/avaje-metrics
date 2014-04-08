package org.avaje.metric;

import java.util.Collection;

import org.avaje.metric.core.DefaultMetricManager;

/**
 * Manages the metrics creation and registration.
 * <p>
 * Provides methods to allow agents to go through the registered metrics and
 * gather/report the statistics.
 * </p>
 */
public class MetricManager {

  private static final DefaultMetricManager mgr = new DefaultMetricManager();

  
  /**
   * Return a MetricNameCache for the given class.
   * <p>
   * The MetricNameCache can be used to derive MetricName objects dynamically
   * with relatively less overhead.
   * </p>
   */
  public static MetricNameCache getMetricNameCache(Class<?> klass) {
    return mgr.getMetricNameCache(klass);
  }

  /**
   * Return a MetricNameCache for a given base metric name.
   * <p>
   * The MetricNameCache can be used to derive MetricName objects dynamically
   * with relatively less overhead.
   * </p>
   */
  public static MetricNameCache getMetricNameCache(MetricName baseName) {
    return mgr.getMetricNameCache(baseName);
  }


  /**
   * Return a TimedMetric using the Class, name to derive the MetricName.
   */
  public static TimedMetric getTimedMetric(Class<?> cls, String eventName) {
    return getTimedMetric(new MetricName(cls, eventName));
  }

  /**
   * Return a TimedMetric given the name.
   */
  public static TimedMetric getTimedMetric(MetricName name) {
    return mgr.getTimedMetric(name);
  }

  /**
   * Return a TimedMetric given the name.
   */
  public static TimedMetric getTimedMetric(String name) {
    return mgr.getTimedMetric(name);
  }
  
  /**
   * Return a CounterMetric given the name.
   */
  public static CounterMetric getCounterMetric(MetricName name) {
    return mgr.getCounterMetric(name);
  }

  /**
   * Return a CounterMetric using the Class and name to derive the MetricName.
   */
  public static CounterMetric getCounterMetric(Class<?> cls, String eventName) {
    return getCounterMetric(new MetricName(cls, eventName));
  }

  /**
   * Return a ValueMetric using the Class and name to derive the MetricName.
   */
  public static ValueMetric getValueMetric(Class<?> cls, String eventName) {
    return getValueMetric(new MetricName(cls, eventName));
  }

  /**
   * Return a ValueMetric given the name.
   */
  public static ValueMetric getValueMetric(MetricName name) {
    return mgr.getValueMetric(name);
  }

  /**
   * Return the TimedMetricGroup with default rateUnit of Minutes and using the default clock.
   */
  public static TimedMetricGroup getTimedMetricGroup(MetricName baseName) {
    return new TimedMetricGroup(baseName);
  }
 
  /**
   * Return a TimedMetricGroup with a common group and type name.
   * 
   * @param group
   *          the common group name
   * @param type
   *          the common type name
   * @return the TimedMetricGroup used to create TimedMetric's that have a
   *         common base name.
   */
  public static TimedMetricGroup getTimedMetricGroup(String group, String type) {
    return new TimedMetricGroup(MetricName.createBaseName(group, type));
  }

  /**
   * Clear the registered metrics.
   */
  protected static void clear() {
    mgr.clear();
  }

  /**
   * Return all the non-jvm registered metrics.
   */
  public static Collection<Metric> getMetrics() {
    return mgr.getMetrics();
  }

  /**
   * Return all the non-jvm registered metrics that are not empty.
   */
  public static Collection<Metric> collectNonEmptyMetrics() {
    return mgr.collectNonEmptyMetrics();
  }
  
  /**
   * Return the core JVM metrics.
   */
  public static Collection<Metric> getJvmMetrics() {
    return mgr.getJvmMetrics();
  }

}
