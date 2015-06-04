package org.avaje.metric.core;

import org.avaje.metric.*;
import org.avaje.metric.core.noop.NoopBucketTimedFactory;
import org.avaje.metric.core.noop.NoopCounterMetricFactory;
import org.avaje.metric.core.noop.NoopTimedMetricFactory;
import org.avaje.metric.core.noop.NoopValueMetricFactory;
import org.avaje.metric.jvm.JvmGarbageCollectionMetricGroup;
import org.avaje.metric.jvm.JvmMemoryMetricGroup;
import org.avaje.metric.jvm.JvmSystemMetricGroup;
import org.avaje.metric.jvm.JvmThreadMetricGroup;
import org.avaje.metric.spi.PluginMetricManager;
import org.avaje.metric.util.PropertiesLoader;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of the PluginMetricManager.
 */
public class DefaultMetricManager implements PluginMetricManager {

  public static final String APPLICATION_PROPERTIES_LOCATIONS = "application.properties.locations";

  public static final String METRICS_COLLECTION_DISABLE = "metrics.collection.disable";

  private final Object monitor = new Object();

  /**
   * Cache of the code JVM metrics.
   */
  private final ConcurrentHashMap<String, Metric> coreJvmMetrics = new ConcurrentHashMap<String, Metric>();

  /**
   * Derived collection of the core jvm metrics.
   */
  private final Collection<Metric> coreJvmMetricCollection;

  /**
   * Cache of the created metrics (excluding JVM metrics).
   */
  private final ConcurrentHashMap<String, Metric> metricsCache = new ConcurrentHashMap<String, Metric>();

  /**
   * Factory for creating TimedMetrics.
   */
  private final MetricFactory<BucketTimedMetric> bucketTimedMetricFactory;

  /**
   * Factory for creating TimedMetrics.
   */
  private final MetricFactory<TimedMetric> timedMetricFactory;

  /**
   * Factory for creating CounterMetrics.
   */
  private final MetricFactory<CounterMetric> counterMetricFactory;

  /**
   * Factory for creating ValueMetrics.
   */
  private final MetricFactory<ValueMetric> valueMetricFactory;

  /**
   * Cache of the metric names.
   */
  private final ConcurrentHashMap<String, MetricNameCache> nameCache = new ConcurrentHashMap<String, MetricNameCache>();

  /**
   * Set to true if collection should be disabled.
   */
  protected final boolean disable;

  public DefaultMetricManager() {

    this.disable = isDisableCollection();

    this.bucketTimedMetricFactory = initBucketTimedFactory(disable);
    this.timedMetricFactory = initTimedMetricFactory(disable);
    this.valueMetricFactory = initValueMetricFactory(disable);
    this.counterMetricFactory = initCounterMetricFactory(disable);

    if (!disable) {
      registerStandardJvmMetrics();
    }
    this.coreJvmMetricCollection = Collections.unmodifiableCollection(coreJvmMetrics.values());
  }

  /**
   * Return true if metric collection should be disabled. 
   * This has the effect that NOOP metric implementations are used.
   */
  private static boolean isDisableCollection() {

    // try system properties first
    String disable = System.getProperty(METRICS_COLLECTION_DISABLE);
    if (disable == null) {
      // now try to read properties files (as defined in application.properties.locations)
      Properties appProperties = PropertiesLoader.indirectLoad(APPLICATION_PROPERTIES_LOCATIONS);
      disable = appProperties.getProperty(METRICS_COLLECTION_DISABLE);
    }

    return "true".equalsIgnoreCase(disable);
  }

  public void reportTiming(List<RequestTimingEntry> timingEntryList) {

    System.out.println("---");
    for (RequestTimingEntry timingEntry : timingEntryList) {
      System.out.println(timingEntry);
    }
    System.out.println("---");

  }


  /**
   * Return the factory used to create TimedMetric instances.
   */
  protected MetricFactory<BucketTimedMetric> initBucketTimedFactory(boolean disableCollection) {
    return (disableCollection) ? new NoopBucketTimedFactory() : new BucketTimedMetricFactory();
  }
  
  /**
   * Return the factory used to create TimedMetric instances.
   */
  protected MetricFactory<TimedMetric> initTimedMetricFactory(boolean disableCollection) {
    return (disableCollection) ? new NoopTimedMetricFactory() : new TimedMetricFactory();
  }

  /**
   * Return the factory used to create CounterMetric instances.
   */
  protected MetricFactory<CounterMetric> initCounterMetricFactory(boolean disableCollection) {
    return (disableCollection) ? new NoopCounterMetricFactory() : new CounterMetricFactory();
  }

  /**
   * Return the factory used to create ValueMetric instances.
   */
  protected MetricFactory<ValueMetric> initValueMetricFactory(boolean disableCollection) {
    return (disableCollection) ? new NoopValueMetricFactory() : new ValueMetricFactory();
  }

  /**
   * Register the standard JVM metrics.
   */
  private void registerStandardJvmMetrics() {

    registerJvmMetric(JvmMemoryMetricGroup.createHeapGroup());
    registerJvmMetric(JvmMemoryMetricGroup.createNonHeapGroup());

    GaugeLongGroup[] gaugeMetricGroups = JvmGarbageCollectionMetricGroup.createGauges();
    for (GaugeLongGroup gaugeMetricGroup : gaugeMetricGroups) {
      registerJvmMetric(gaugeMetricGroup);
    }

    registerJvmMetric(JvmThreadMetricGroup.createThreadMetricGroup());
    registerJvmMetric(JvmSystemMetricGroup.getUptime());

    DefaultGaugeDoubleMetric osLoadAvgMetric = JvmSystemMetricGroup.getOsLoadAvgMetric();
    if (osLoadAvgMetric.getValue() >= 0) {
      // OS Load Average is supported on this system
      registerJvmMetric(osLoadAvgMetric);
    }
  }

  private void registerJvmMetric(Metric m) {
    coreJvmMetrics.put(m.getName().getSimpleName(), m);
  }

  @Override
  public MetricName name(String name) {
    return DefaultMetricName.parse(name);
  }

  @Override
  public MetricName name(String group, String type, String name) {
    return new DefaultMetricName(group, type, name);
  }

  @Override
  public MetricName name(Class<?> cls, String name) {
    return new DefaultMetricName(cls, name);
  }

  @Override
  public MetricNameCache getMetricNameCache(Class<?> klass) {
    return getMetricNameCache(name(klass, null));
  }

  @Override
  public MetricNameCache getMetricNameCache(MetricName baseName) {

    String key = baseName.getSimpleName();
    MetricNameCache metricNameCache = nameCache.get(key);
    if (metricNameCache == null) {
      metricNameCache = new DefaultMetricNameCache(baseName);
      MetricNameCache oldNameCache = nameCache.putIfAbsent(key, metricNameCache);
      if (oldNameCache != null) {
        return oldNameCache;
      }
    }
    return metricNameCache;
  }

  @Override
  public TimedMetricGroup getTimedMetricGroup(MetricName baseName) {
    return new DefaultTimedMetricGroup(baseName);
  }

  @Override
  public TimedMetric getTimedMetric(MetricName name) {
    return (TimedMetric) getMetric(name, timedMetricFactory);
  }

  
  @Override
  public BucketTimedMetric getBucketTimedMetric(MetricName name, int... bucketRanges) {
    return (BucketTimedMetric) getMetric(name, bucketTimedMetricFactory, bucketRanges);
  }

  @Override
  public CounterMetric getCounterMetric(MetricName name) {
    return (CounterMetric) getMetric(name, counterMetricFactory);
  }

  @Override
  public ValueMetric getValueMetric(MetricName name) {
    return (ValueMetric) getMetric(name, valueMetricFactory);
  }

  @Override
  public GaugeDoubleMetric register(MetricName name, GaugeDouble gauge) {

    DefaultGaugeDoubleMetric metric = new DefaultGaugeDoubleMetric(name, gauge);
    metricsCache.put(name.getSimpleName(), metric);
    return metric;
  }

  @Override
  public GaugeLongMetric register(MetricName name, GaugeLong gauge) {

    DefaultGaugeLongMetric metric = new DefaultGaugeLongMetric(name, gauge);
    metricsCache.put(name.getSimpleName(), metric);
    return metric;
  }
  
  private Metric getMetric(MetricName name, MetricFactory<?> factory) {
    return getMetric(name, factory, null);
  }
  
  private Metric getMetric(MetricName name, MetricFactory<?> factory, int[] bucketRanges) {

    String cacheKey = name.getSimpleName();
    // try lock free get first
    Metric metric = metricsCache.get(cacheKey);
    if (metric == null) {
      synchronized (monitor) {
        // use synchronized block
        metric = metricsCache.get(cacheKey);
        if (metric == null) {
          metric = factory.createMetric(name, bucketRanges);
          metricsCache.put(cacheKey, metric);
        }
      }
    }
    return metric;
  }
  
  public void clear() {
    synchronized (monitor) {
      metricsCache.clear();
    }
  }

  @Override
  public Collection<Metric> collectNonEmptyMetrics() {
    synchronized (monitor) {

      Collection<Metric> values = metricsCache.values();
      List<Metric> list = new ArrayList<Metric>(values.size());

      for (Metric metric : values) {
        if (metric.collectStatistics()) {
          list.add(metric);
        }
      }

      return Collections.unmodifiableList(list);
    }
  }

  @Override
  public Collection<Metric> getMetrics() {
    synchronized (monitor) {
      return Collections.unmodifiableCollection(metricsCache.values());
    }
  }

  @Override
  public Collection<Metric> getJvmMetrics() {
    return coreJvmMetricCollection;
  }

}
