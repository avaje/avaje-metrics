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
   * Force the statistics to be updated.
   * <p>
   * Typically this is not called but left to the underlying MetricManager to
   * update the statistics periodically using a background thread.
   * </p>
   */
  public static void updateStatistics() {
    mgr.updateStatistics();
  }
  
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
   * Return a TimedMetric given its metricName.
   */
  public static TimedMetric getTimedMetric(MetricName metricName) {
    return getTimedMetric(metricName, null);
  }

  /**
   * Return a TimedMetric using the Class, name to derive the MetricName.
   */
  public static TimedMetric getTimedMetric(Class<?> cls, String eventName) {
    return getTimedMetric(new MetricName(cls, eventName), null);
  }

  /**
   * Return a TimedMetric given the name, rateUnit and clock.
   */
  public static TimedMetric getTimedMetric(MetricName name, Clock clock) {
    return mgr.getTimedMetric(name, clock);
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
   * Return a LoadMetric given the name.
   */
  public static LoadMetric getLoadMetric(MetricName name) {
    return mgr.getLoadMetric(name);
  }

  /**
   * Return a LoadMetric using the Class and name to derive the MetricName.
   */
  public static LoadMetric getLoadMetric(Class<?> cls, String eventName) {
    return getLoadMetric(new MetricName(cls, eventName));
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
   * Return a TimedMetricGroup.
   * <p>
   * This is used when a group of TimedMetric's have a common base name,
   * rateUnit and clock. These TimedMetric's only differ by the name (all share
   * the same group and type etc).
   * </p>
   * 
   * @param baseName
   *          the common part of the metric name
   * @param rateUnit
   *          the rateUnit for all the TimedMetric's
   * @param clock
   *          the clock to use
   * 
   * @return the TimedMetricGroup used to create TimedMetric's that have a
   *         common base name.
   */
  public static TimedMetricGroup getTimedMetricGroup(MetricName baseName, Clock clock) {
    return new TimedMetricGroup(baseName, clock);
  }

  /**
   * Return a TimedMetricGroup with a common group and type name.
   * <p>
   * This uses the default clock.
   * </p>
   * 
   * @param group
   *          the common group name
   * @param type
   *          the common type name
   * @param rateUnit
   *          the rateUnit used
   * @return the TimedMetricGroup used to create TimedMetric's that have a
   *         common base name.
   */
  public static TimedMetricGroup getTimedMetricGroup(String group, String type) {
    return new TimedMetricGroup(MetricName.createBaseName(group, type), Clock.defaultClock());
  }

  /**
   * Register a visit only metric with the MetricManager.
   */
  public static void registerVisitOnly(Metric metric) {
    mgr.registerVisitOnly(metric);
  }
  
  /**
   * Clear the registered metrics.
   */
  protected static void clear() {
    mgr.clear();
  }

  /**
   * Return all the metrics registered.
   */
  public static Collection<Metric> getMetrics() {
    return mgr.getMetrics();
  }

  /**
   * Return all the metrics registered.
   */
  public static Collection<Metric> getVisitOnlyMetrics() {
    return mgr.getVisitOnlyMetrics();
  }
  
  /**
   * Visit all the metrics.
   */
  public static void visitAll(MetricVisitor visitor) {

    Collection<Metric> visitOnlyMetrics = mgr.getVisitOnlyMetrics();
    for (Metric metric : visitOnlyMetrics) {
      metric.visit(visitor);
    }
    
    Collection<Metric> metrics = mgr.getMetrics();
    for (Metric metric : metrics) {
      metric.visit(visitor);
    }
  }

  /**
   * Visit all the metrics that match the matcher.
   */
  public static void visit(MetricMatcher matcher, MetricVisitor visitor) {

    Collection<Metric> metrics = mgr.getMetrics();
    for (Metric metric : metrics) {
      if (matcher.isMatch(metric)) {
        metric.visit(visitor);
      }
    }
  }

}
