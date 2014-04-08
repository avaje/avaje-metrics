package org.avaje.metric.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.avaje.metric.CounterMetric;
import org.avaje.metric.GaugeMetric;
import org.avaje.metric.GaugeMetricGroup;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;
import org.avaje.metric.MetricNameCache;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.ValueMetric;
import org.avaje.metric.jvm.JvmGarbageCollectionMetricGroup;
import org.avaje.metric.jvm.JvmMemoryMetricGroup;
import org.avaje.metric.jvm.JvmSystemMetricGroup;
import org.avaje.metric.jvm.JvmThreadMetricGroup;

public class DefaultMetricManager {

  private final String monitor = new String();

  private final ConcurrentHashMap<String, Metric> coreJvmMetrics = new ConcurrentHashMap<String, Metric>();
  private final Collection<Metric> coreJvmMetricCollection;
  
  private final ConcurrentHashMap<String, Metric> metricsMap = new ConcurrentHashMap<String, Metric>();

  private final MetricFactory<TimedMetric> timedMetricFactory = new TimedMetricFactory();
  private final MetricFactory<CounterMetric> counterMetricFactory = new CounterMetricFactory();
  private final MetricFactory<ValueMetric> valueMetricFactory = new ValueMetricFactory();


  private final ConcurrentHashMap<String, MetricNameCache> nameCache = new ConcurrentHashMap<String, MetricNameCache>();

  public DefaultMetricManager() {
    
    registerStandardJvmMetrics();
    this.coreJvmMetricCollection = Collections.unmodifiableCollection(coreJvmMetrics.values()); 
  }
  
  private void registerStandardJvmMetrics() {
    
    registerJvmMetric(JvmMemoryMetricGroup.createHeapGroup());
    registerJvmMetric(JvmMemoryMetricGroup.createNonHeapGroup());
    
    GaugeMetricGroup[] gaugeMetricGroups = JvmGarbageCollectionMetricGroup.createGauges();
    for (GaugeMetricGroup gaugeMetricGroup : gaugeMetricGroups) {
      registerJvmMetric(gaugeMetricGroup);
    }
    
    registerJvmMetric(JvmThreadMetricGroup.createThreadMetricGroup());
    registerJvmMetric(JvmSystemMetricGroup.getUptime());
    
    GaugeMetric osLoadAvgMetric = JvmSystemMetricGroup.getOsLoadAvgMetric();
    if (osLoadAvgMetric.getValue() >= 0) {
      // OS Load Average is supported on this system
      registerJvmMetric(osLoadAvgMetric);
    }
  }

  private void registerJvmMetric(Metric m) {
    coreJvmMetrics.put(m.getName().getMBeanName(), m);
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

  public TimedMetric getTimedMetric(String name) {
    return (TimedMetric) getMetric(MetricName.parse(name), timedMetricFactory);
  }
  
  public TimedMetric getTimedMetric(MetricName name) {
    return (TimedMetric) getMetric(name, timedMetricFactory);
  }

  public CounterMetric getCounterMetric(MetricName name) {
    return (CounterMetric) getMetric(name, counterMetricFactory);
  }
  
  public ValueMetric getValueMetric(MetricName name) {
    return (ValueMetric) getMetric(name, valueMetricFactory);
  }

  private Metric getMetric(MetricName name, MetricFactory<?> factory) {

    String cacheKey = name.getMBeanName();
    // try lock free get first
    Metric metric = metricsMap.get(cacheKey);
    if (metric == null) {
      synchronized (monitor) {
        // use synchronised block
        metric = metricsMap.get(cacheKey);
        if (metric == null) {
          metric = factory.createMetric(name);
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

  public Collection<Metric> collectNonEmptyMetrics() {
    synchronized (monitor) {
      List<Metric> list = new ArrayList<Metric>();
      Collection<Metric> values = metricsMap.values();
      for (Metric metric : values) {
        if (!metric.collectStatistics()) {
          list.add(metric);
        }
      }
      
      return Collections.unmodifiableList(list);
    }
  }
  
  
  public Collection<Metric> getMetrics() {
    synchronized (monitor) {
      return Collections.unmodifiableCollection(metricsMap.values());
    }
  }
  
  public Collection<Metric> getJvmMetrics() {
    return coreJvmMetricCollection;
  }

}
