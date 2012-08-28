package org.avaje.metric.core;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.avaje.metric.Clock;
import org.avaje.metric.CounterMetric;
import org.avaje.metric.GaugeMetricGroup;
import org.avaje.metric.LoadMetric;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;
import org.avaje.metric.MetricNameCache;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.ValueMetric;
import org.avaje.metric.jvm.JvmGarbageCollectionMetricGroup;
import org.avaje.metric.jvm.JvmMemoryMetricGroup;
import org.avaje.metric.jvm.JvmThreadMetricGroup;

public class DefaultMetricManager {

  private final String monitor = new String();

  private final ConcurrentHashMap<String, Metric> visitOnlyMetrics = new ConcurrentHashMap<String, Metric>();

  private final ConcurrentHashMap<String, Metric> metricsMap = new ConcurrentHashMap<String, Metric>();

  private final Timer timer = new Timer("MetricManager", true);

  //private final JmxMetricRegister jmxRegistry = new JmxMetricRegister();

  private final MetricFactory timedMetricFactory = new TimedMetricFactory();
  private final MetricFactory counterMetricFactory = new CounterMetricFactory();
  private final MetricFactory loadMetricFactory = new LoadMetricFactory();
  private final MetricFactory valueMetricFactory = new ValueMetricFactory();


  private final ConcurrentHashMap<String, MetricNameCache> nameCache = new ConcurrentHashMap<String, MetricNameCache>();

  public DefaultMetricManager() {
    
    timer.scheduleAtFixedRate(new UpdateStatisticsTask(), 5 * 1000, 2 * 1000);
    
    registerJvmMetrics();
  }
  
  private void registerJvmMetrics() {
    
    JvmMemoryMetricGroup jvmMem = new JvmMemoryMetricGroup();
    registerVisitOnly(jvmMem.getHeapGroup());
    registerVisitOnly(jvmMem.getNonHeapGroup());
    
    JvmGarbageCollectionMetricGroup jvmGc = new JvmGarbageCollectionMetricGroup();
    GaugeMetricGroup[] gaugeMetricGroups = jvmGc.getGaugeMetricGroups();
    for (GaugeMetricGroup gaugeMetricGroup : gaugeMetricGroups) {
      registerVisitOnly(gaugeMetricGroup);
    }
    
    JvmThreadMetricGroup jvmThreads = new JvmThreadMetricGroup();
    registerVisitOnly(jvmThreads.getGaugeMetricGroup());
  }

  private class UpdateStatisticsTask extends TimerTask {

    @Override
    public void run() {
      updateStatistics();
    }
  }

  public MetricNameCache getMetricNameCache(Class<?> klass) {
    return getMetricNameCache(new MetricName(klass, null));
  }
  
  public MetricNameCache getMetricNameCache(MetricName baseName) {
   
    String key = baseName.getMBeanName();
    MetricNameCache metricNameCache = nameCache.get(key);
    if (metricNameCache == null) {
      metricNameCache = new MetricNameCache(baseName);
      MetricNameCache oldNameCache = nameCache.putIfAbsent(key, metricNameCache);
      if (oldNameCache != null) {
        return oldNameCache;
      }
    }
    return metricNameCache;
  }

  public void updateStatistics() {
    Collection<Metric> allMetrics = getMetrics();
    for (Metric metric : allMetrics) {
      metric.updateStatistics();
    }
  }

  public TimedMetric getTimedMetric(MetricName name, Clock clock) {
    return (TimedMetric) getMetric(name, clock, timedMetricFactory);
  }

  public CounterMetric getCounterMetric(MetricName name) {
    return (CounterMetric) getMetric(name, null, counterMetricFactory);
  }
  
  public LoadMetric getLoadMetric(MetricName name) {
    return (LoadMetric) getMetric(name, null, loadMetricFactory);
  }

  public ValueMetric getValueMetric(MetricName name) {
    return (ValueMetric) getMetric(name, null, valueMetricFactory);
  }
  
  public void registerVisitOnly(Metric m) {
    String cacheKey = m.getName().getMBeanName();
    visitOnlyMetrics.put(cacheKey, m);
  }
  
//  private void registerGcMetrics() {
//    for (LoadMetric m : gcLoadMetrics) {
//      String cacheKey = m.getName().getMBeanName();
//      metricsMap.put(cacheKey, m);
//    }
//  }

  private Metric getMetric(MetricName name, Clock clock, MetricFactory factory) {

    String cacheKey = name.getMBeanName();
    // try lock free get first
    Metric metric = metricsMap.get(cacheKey);
    if (metric == null) {
      synchronized (monitor) {
        // use synchronised block
        metric = metricsMap.get(cacheKey);
        if (metric == null) {
          metric = factory.createMetric(name, clock);
          metricsMap.put(cacheKey, metric);
        }
      }
    }
    return metric;
  }

  public void clear() {
    synchronized (monitor) {
      metricsMap.clear();
    }
  }

  public Collection<Metric> getMetrics() {
    synchronized (monitor) {
      return metricsMap.values();
    }
  }
  
  public Collection<Metric> getVisitOnlyMetrics() {
    return visitOnlyMetrics.values();
  }

}
