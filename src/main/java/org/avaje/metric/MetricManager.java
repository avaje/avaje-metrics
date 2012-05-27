package org.avaje.metric;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.avaje.metric.core.DefaultMetricManager;


public class MetricManager {

  private static final DefaultMetricManager mgr = new DefaultMetricManager();
 
  public static void updateStatistics() {
    mgr.updateStatistics();
  }

  public static TimedMetric getTimedMetric(Class<?> cls, String eventName, String scope, TimeUnit rateUnit) {
    return getTimedMetric(new MetricName(cls, eventName, scope), rateUnit, null);
  }
  
//  public static TimedMetric getTimedMetric(String metricName, String scope, TimeUnit rateUnit) {
//    return getTimedMetric(new MetricName(metricName, scope), rateUnit, null);
//  }

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
  
  public static TimedMetric getTimedMetric(MetricName name, TimeUnit rateUnit, Clock clock ) {
    return mgr.getTimedMetric(name, rateUnit, clock);
  }
  
  protected static void clear() {
    mgr.clear();
  }
  
  public static Collection<Metric> getAllMetrics() {
    return mgr.getAllMetrics();
  }

}
