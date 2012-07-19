package org.avaje.metric;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.avaje.metric.core.DefaultMetricManager;
import org.avaje.metric.jvm.GarbageCollectionRateCollection;

public class MetricManager {

  private static final DefaultMetricManager mgr = new DefaultMetricManager();
  static {
    mgr.addGcRateCollection();
  }

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

  public static TimedMetric getTimedMetric(Class<?> cls, String eventName, String scope, TimeUnit rateUnit) {
    return getTimedMetric(new MetricName(cls, eventName, scope), rateUnit, null);
  }

  public static TimedMetric getTimedMetric(MetricName key) {
    return getTimedMetric(key, null, null);
  }

  public static EventMetric getEventMetric(Class<?> cls, String eventName, TimeUnit rateUnit) {
    return getEventMetric(cls, eventName, null, rateUnit);
  }

  public static EventMetric getEventMetric(Class<?> cls, String eventName, String scope, TimeUnit rateUnit) {
    return mgr.getEventMetric(new MetricName(cls, eventName, scope), rateUnit);
  }

  public static EventMetric getEventMetric(MetricName name, TimeUnit rateUnit) {
    return mgr.getEventMetric(name, rateUnit);
  }

  public static ValueMetric getValueMetric(MetricName name, TimeUnit rateUnit) {
    return mgr.getValueMetric(name, rateUnit);
  }

  public static TimedMetric getTimedMetric(MetricName name, TimeUnit rateUnit, Clock clock) {
    return mgr.getTimedMetric(name, rateUnit, clock);
  }

  protected static void clear() {
    mgr.clear();
  }

  public static Collection<Metric> getAllMetrics() {
    return mgr.getAllMetrics();
  }
  
  public static GarbageCollectionRateCollection getGarbageCollectionRateCollection() {
    return mgr.getGarbageCollectionRateCollection();
  }

}
