package org.avaje.metric;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

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
   * Return a TimedMetric using the Class, name and scope to derive the
   * MetricName.
   */
  public static TimedMetric getTimedMetric(Class<?> cls, String eventName, String scope,
      TimeUnit rateUnit) {
    return getTimedMetric(new MetricName(cls, eventName, scope), rateUnit, null);
  }

  /**
   * Return a TimedMetric given its metricName.
   */
  public static TimedMetric getTimedMetric(MetricName metricName) {
    return getTimedMetric(metricName, null, null);
  }

  /**
   * Return a EventMetric using the Class and name to derive the MetricName.
   */
  public static EventMetric getEventMetric(Class<?> cls, String eventName, TimeUnit rateUnit) {
    return getEventMetric(cls, eventName, null, rateUnit);
  }

  /**
   * Return a EventMetric using the Class, name and scope to derive the
   * MetricName.
   */
  public static EventMetric getEventMetric(Class<?> cls, String eventName, String scope,
      TimeUnit rateUnit) {
    return mgr.getEventMetric(new MetricName(cls, eventName, scope), rateUnit);
  }

  /**
   * Return a EventMetric given the name and rateUnit.
   */
  public static EventMetric getEventMetric(MetricName name, TimeUnit rateUnit) {
    return mgr.getEventMetric(name, rateUnit);
  }

  /**
   * Return a ValueMetric given the name, rateUnit.
   */
  public static ValueMetric getValueMetric(MetricName name, TimeUnit rateUnit) {
    return mgr.getValueMetric(name, rateUnit);
  }

  /**
   * Return a TimedMetric given the name, rateUnit and clock.
   */
  public static TimedMetric getTimedMetric(MetricName name, TimeUnit rateUnit, Clock clock) {
    return mgr.getTimedMetric(name, rateUnit, clock);
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
  public static Collection<Metric> getAllMetrics() {
    return mgr.getAllMetrics();
  }

  /**
   * Visit all the metrics.
   */
  public static void visitAll(MetricVisitor visitor) {

    Collection<Metric> metrics = mgr.getAllMetrics();
    for (Metric metric : metrics) {
      metric.visit(visitor);
    }
  }

  /**
   * Visit all the metrics that match the matcher.
   */
  public static void visit(MetricMatcher matcher, MetricVisitor visitor) {

    Collection<Metric> metrics = mgr.getAllMetrics();
    for (Metric metric : metrics) {
      if (matcher.isMatch(metric)) {
        metric.visit(visitor);
      }
    }
  }

}
