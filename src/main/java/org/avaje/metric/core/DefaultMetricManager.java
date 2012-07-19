package org.avaje.metric.core;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.management.ObjectName;

import org.avaje.metric.*;
import org.avaje.metric.jvm.GarbageCollectionRateCollection;

public class DefaultMetricManager {

  private final String monitor = new String();

  private final ConcurrentHashMap<String, Metric> concMetricMap = new ConcurrentHashMap<String, Metric>();

  private final Timer timer = new Timer("MetricManager", true);

  private final JmxMetricRegister jmxRegistry = new JmxMetricRegister();

  private final MetricFactory timedMetricFactory = new TimedMetricFactory();
  private final MetricFactory eventMetricFactory = new EventMetricFactory();
  private final MetricFactory valueMetricFactory = new ValueMetricFactory();

  private final LoadMetric[] gcLoadMetrics;
  
  private final ConcurrentHashMap<String, MetricNameCache> nameCache = new ConcurrentHashMap<String, MetricNameCache>();
  
  public DefaultMetricManager() {
    timer.scheduleAtFixedRate(new UpdateStatisticsTask(), 5 * 1000, 2 * 1000);

    GarbageCollectionRateCollection gc = new GarbageCollectionRateCollection(timer);
    gcLoadMetrics = gc.getGarbageCollectorsLoadMetrics();
    registerGcMetrics();
  }

  private class UpdateStatisticsTask extends TimerTask {

    @Override
    public void run() {
      updateStatistics();
    }
  }

  public MetricNameCache getMetricNameCache(Class<?> klass) {
    
    String key = klass.getName().replaceAll("\\$$", "");
    MetricNameCache metricNameCache = nameCache.get(key);
    if (metricNameCache == null){
      metricNameCache = new MetricNameCache(klass);
      MetricNameCache oldNameCache = nameCache.putIfAbsent(key, metricNameCache);
      if (oldNameCache != null) {
        return oldNameCache;
      }
    }
    return metricNameCache;
  }
  
  public void updateStatistics() {
    Collection<Metric> allMetrics = getAllMetrics();
    for (Metric metric : allMetrics) {
      metric.updateStatistics();
    }
  }

  public TimedMetric getTimedMetric(MetricName name, TimeUnit rateUnit, Clock clock) {
    return (TimedMetric) getMetric(name, rateUnit, clock, timedMetricFactory);
  }
  
  public EventMetric getEventMetric(MetricName name, TimeUnit rateUnit) {
    return (EventMetric) getMetric(name, rateUnit, null, eventMetricFactory);
  }

  public ValueMetric getValueMetric(MetricName name, TimeUnit rateUnit) {
    return (ValueMetric) getMetric(name, rateUnit, null, valueMetricFactory);
  }
  

  private void registerGcMetrics() {
    for (LoadMetric m : gcLoadMetrics) {
      String cacheKey = m.getName().getMBeanName();
      concMetricMap.put(cacheKey, m);
    }
  }

  private Metric getMetric(MetricName name, TimeUnit rateUnit, Clock clock, MetricFactory factory) {

    String cacheKey = name.getMBeanName();
    // try lock free get first
    Metric metric = concMetricMap.get(cacheKey);
    if (metric == null) {
      synchronized (monitor) {
        // use synchronised block
        metric = concMetricMap.get(cacheKey);
        if (metric == null) {
          metric = factory.createMetric(name, rateUnit, clock);
          concMetricMap.put(cacheKey, metric);
        }
      }
    }
    return metric;
  }

  public void clear() {
    synchronized (monitor) {
      Collection<Metric> values = concMetricMap.values();
      for (Metric metric : values) {
        jmxRegistry.unregister(metric.getName().getMBeanObjectName());
        if (metric instanceof TimedMetric) {
          ObjectName errorMBeanName = ((TimedMetric) metric).getErrorMBeanName();
          jmxRegistry.unregister(errorMBeanName);
        }

      }
      concMetricMap.clear();
    }
  }

  public Collection<Metric> getAllMetrics() {
    synchronized (monitor) {
      return concMetricMap.values();
    }
  }

}
