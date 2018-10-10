package org.avaje.metric.core;

import org.avaje.metric.CounterMetric;
import org.avaje.metric.GaugeDouble;
import org.avaje.metric.GaugeDoubleMetric;
import org.avaje.metric.GaugeLong;
import org.avaje.metric.GaugeLongMetric;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;
import org.avaje.metric.MetricNameCache;
import org.avaje.metric.RequestTiming;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.TimedMetricGroup;
import org.avaje.metric.TimingMetricInfo;
import org.avaje.metric.ValueMetric;
import org.avaje.metric.core.noop.NoopBucketTimedFactory;
import org.avaje.metric.core.noop.NoopCounterMetricFactory;
import org.avaje.metric.core.noop.NoopTimedMetricFactory;
import org.avaje.metric.core.noop.NoopValueMetricFactory;
import org.avaje.metric.core.spi.ExternalRequestIdAdapter;
import org.avaje.metric.spi.PluginMetricManager;
import org.avaje.metric.statistics.MetricStatistics;
import org.avaje.metric.util.LikeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Default implementation of the PluginMetricManager.
 */
public class DefaultMetricManager implements PluginMetricManager {

  private static final Logger logger = LoggerFactory.getLogger(DefaultMetricManager.class);

  private static final String METRICS_MDC_REQUEST_ID = "metrics.mdc.requestId";

  private static final String JVM = "jvm";

  private final NameComp sortByName = new NameComp();

  private final Object monitor = new Object();

  /**
   * Cache of the code JVM metrics.
   */
  private final ConcurrentHashMap<String, Metric> coreJvmMetrics = new ConcurrentHashMap<>();

  /**
   * Cache of the created metrics (excluding JVM metrics).
   */
  private final ConcurrentHashMap<String, Metric> metricsCache = new ConcurrentHashMap<>();

  /**
   * Factory for creating TimedMetrics.
   */
  private final MetricFactory<TimedMetric> bucketTimedMetricFactory;

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
  private final ConcurrentHashMap<String, MetricNameCache> nameCache = new ConcurrentHashMap<>();

  private final ConcurrentLinkedQueue<RequestTiming> requestTimings = new ConcurrentLinkedQueue<>();

  /**
   * Adapter that can obtain a request id to associate with request timings.
   */
  protected final ExternalRequestIdAdapter externalRequestIdAdapter;

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
    this.externalRequestIdAdapter = initExternalRequestIdAdapter(disable);
  }

  private static ExternalRequestIdAdapter initExternalRequestIdAdapter(boolean disable) {

    if (disable) return null;

    String mdcKey = System.getProperty(METRICS_MDC_REQUEST_ID);
    if (mdcKey != null) {
      return new MdcExternalRequestIdAdapter(mdcKey);
    }

    return null;
  }

  /**
   * Return true if metric collection should be disabled.
   * This has the effect that NOOP metric implementations are used.
   */
  private static boolean isDisableCollection() {

    String disable = System.getProperty("metrics.disable", System.getenv("METRICS_DISABLE"));
    return "true".equalsIgnoreCase(disable);
  }

  @Override
  public void reportTiming(RequestTiming requestTiming) {

    if (externalRequestIdAdapter != null) {
      String requestId = externalRequestIdAdapter.getExternalRequestId();
      if (requestId != null) {
        requestTiming.setExternalRequestId(requestId);
      }
    }

    requestTimings.add(requestTiming);
  }


  /**
   * Return the factory used to create TimedMetric instances.
   */
  protected static MetricFactory<TimedMetric> initBucketTimedFactory(boolean disableCollection) {
    return (disableCollection) ? new NoopBucketTimedFactory() : new BucketTimedMetricFactory();
  }

  /**
   * Return the factory used to create TimedMetric instances.
   */
  protected static MetricFactory<TimedMetric> initTimedMetricFactory(boolean disableCollection) {
    return (disableCollection) ? new NoopTimedMetricFactory() : new TimedMetricFactory();
  }

  /**
   * Return the factory used to create CounterMetric instances.
   */
  protected static MetricFactory<CounterMetric> initCounterMetricFactory(boolean disableCollection) {
    return (disableCollection) ? new NoopCounterMetricFactory() : new CounterMetricFactory();
  }

  /**
   * Return the factory used to create ValueMetric instances.
   */
  protected static MetricFactory<ValueMetric> initValueMetricFactory(boolean disableCollection) {
    return (disableCollection) ? new NoopValueMetricFactory() : new ValueMetricFactory();
  }

  /**
   * Register the standard JVM metrics.
   */
  @Override
  public void registerStandardJvmMetrics(boolean reportChangesOnly) {

    registerJvmGCMetrics();
    registerJvmMemoryMetrics(reportChangesOnly);
    registerJvmThreadMetrics(reportChangesOnly);
    registerJvmProcessMemoryMetrics(reportChangesOnly);
    registerJvmOsLoadMetric();
  }

  @Override
  public void registerJvmProcessMemoryMetrics(boolean reportChangesOnly) {
    registerAll(JvmProcessMemory.createGauges(reportChangesOnly));
  }

  @Override
  public void registerJvmOsLoadMetric() {
    GaugeDoubleMetric osLoadAvgMetric = JvmSystemMetricGroup.getOsLoadAvgMetric();
    if (osLoadAvgMetric.getValue() >= 0) {
      // OS Load Average is supported on this system
      registerJvmMetric(osLoadAvgMetric);
    }
  }

  @Override
  public void registerJvmThreadMetrics(boolean reportChangesOnly) {
    registerAll(JvmThreadMetricGroup.createThreadMetricGroup(reportChangesOnly));
  }

  @Override
  public void registerJvmGCMetrics() {
    registerAll(JvmGarbageCollectionMetricGroup.createGauges());
  }

  @Override
  public void registerJvmMemoryMetrics(boolean reportChangesOnly) {
    registerAll(JvmMemoryMetricGroup.createHeapGroup(reportChangesOnly));
    registerAll(JvmMemoryMetricGroup.createNonHeapGroup(reportChangesOnly));
  }

  private void registerAll(List<Metric> groups) {
    for (Metric metric : groups) {
      registerJvmMetric(metric);
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
    if (group == null) {
      throw new IllegalArgumentException("group needs to be specified");
    }
    if (type == null) {
      throw new IllegalArgumentException("type needs to be specified for JMX bean name support");
    }
    return name(group + '.' + type + append(name));
  }

  @Override
  public MetricName name(Class<?> cls, String name) {
    return name(cls.getName() + append(name));
  }

  private static String append(String value) {
    return (value == null) ? "" : '.' + value;
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
  public TimedMetric getTimedMetric(MetricName name, int... bucketRanges) {
    return (TimedMetric) getMetric(name, bucketTimedMetricFactory, bucketRanges);
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
    return put(name, new DefaultGaugeDoubleMetric(name, gauge));
  }

  @Override
  public GaugeLongMetric register(MetricName name, GaugeLong gauge) {
    return put(name, (GaugeLongMetric) new DefaultGaugeLongMetric(name, gauge));
  }

  private <T extends Metric> T put(MetricName name, T metric) {
    if (JVM.equals(name.getGroup())) {
      registerJvmMetric(metric);
    } else {
      metricsCache.put(name.getSimpleName(), metric);
    }
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

  private Metric getMetricWithoutCreate(MetricName name) {

    return metricsCache.get(name.getSimpleName());
  }

  public void clear() {
    synchronized (monitor) {
      metricsCache.clear();
    }
  }

  /**
   * Return the request timings that have been collected since the last collection.
   */
  public List<RequestTiming> collectRequestTimings() {

    List<RequestTiming> list = new ArrayList<>();
    RequestTiming req;
    while ((req = requestTimings.poll()) != null) {
      list.add(req);
    }
    return list;
  }

  @Override
  public List<MetricStatistics> collectNonEmptyMetrics() {
    synchronized (monitor) {
      DStatsCollector collector = new DStatsCollector();
      Collection<Metric> values = metricsCache.values();
      for (Metric metric : values) {
        metric.collect(collector);
      }
      return collector.getList();
    }
  }

  @Override
  public Collection<Metric> getMetrics() {
    synchronized (monitor) {
      return Collections.unmodifiableCollection(metricsCache.values());
    }
  }

  @Override
  public boolean setRequestTimingCollection(String fullMetricName, int collectionCount) {

    return setRequestTimingCollection(MetricName.of(fullMetricName), collectionCount);
  }

  @Override
  public boolean setRequestTimingCollection(Class<?> cls, String name, int collectionCount) {

    return setRequestTimingCollection(MetricName.of(cls, name), collectionCount);
  }

  /**
   * Set request timing collection on a specific timed metric.
   * Return false if the metric was not found (and timing collection was not set).
   */
  protected boolean setRequestTimingCollection(MetricName metricName, int collectionCount) {

    Metric metric = getMetricWithoutCreate(metricName);
    if (metric instanceof TimedMetric) {
      TimedMetric timed = (TimedMetric) metric;
      timed.setRequestTimingCollection(collectionCount);
      return true;
    }

    return false;
  }

  /**
   * Set request timing collection on metrics that match the nameMatch expression.
   *
   * @param nameMatch       The expression used to match timing metrics
   * @param collectionCount The number of requests to collect
   * @return The timing metrics that had the request timing collection set
   */
  @Override
  public List<TimingMetricInfo> setRequestTimingCollectionUsingMatch(String nameMatch, int collectionCount) {

    if (nameMatch == null || nameMatch.trim().length() == 0) {
      // not turning on collection globally for empty
      return Collections.EMPTY_LIST;
    }

    LikeMatcher like = new LikeMatcher(nameMatch);

    List<TimingMetricInfo> changes = new ArrayList<>();

    for (Metric metric : metricsCache.values()) {
      if (metric instanceof TimedMetric) {
        TimedMetric timed = (TimedMetric) metric;
        if (like.matches(timed.getName().getSimpleName())) {
          timed.setRequestTimingCollection(collectionCount);
          logger.debug("setRequestTimingCollection({}) on {}", collectionCount, timed.getName().getSimpleName());
          changes.add(new TimingMetricInfo(timed.getName().getSimpleName(), collectionCount));
        }
      }
    }

    // ensure we return them in a predicable order
    Collections.sort(changes, sortByName);
    return changes;
  }

  /**
   * Return currently active timing metrics that match the name expression.
   */
  @Override
  public List<TimingMetricInfo> getRequestTimingMetrics(String nameMatchExpression) {
    return getRequestTimingMetrics(true, nameMatchExpression);
  }

  /**
   * Return the list of all timing metrics that match the name expression.
   */
  @Override
  public List<TimingMetricInfo> getAllTimingMetrics(String nameMatchExpression) {
    return getRequestTimingMetrics(false, nameMatchExpression);
  }

  /**
   * Return the list of timing metrics that we are interested in.
   *
   * @param activeOnly          true if we only want timing metrics that are actively collecting request timings.
   * @param nameMatchExpression the expression used to match/filter metric names. Null or empty means match all.
   */
  protected List<TimingMetricInfo> getRequestTimingMetrics(boolean activeOnly, String nameMatchExpression) {

    synchronized (monitor) {

      List<TimingMetricInfo> list = new ArrayList<>();

      LikeMatcher like = new LikeMatcher(nameMatchExpression);

      for (Metric metric : metricsCache.values()) {
        if (metric instanceof TimedMetric) {
          TimedMetric timed = (TimedMetric) metric;
          if (!activeOnly || timed.getRequestTimingCollection() >= 1) {
            // actively collection or doing the all search
            if (like.matches(timed.getName().getSimpleName())) {
              // metric name matches our expression
              list.add(new TimingMetricInfo(timed.getName().getSimpleName(), timed.getRequestTimingCollection()));
            }
          }
        }
      }

      // ensure we return them in a predicable order
      Collections.sort(list, sortByName);
      return list;
    }
  }

  @Override
  public Collection<Metric> getJvmMetrics() {
    return coreJvmMetrics.values();
  }

  @Override
  public List<MetricStatistics> collectNonEmptyJvmMetrics() {

    DStatsCollector collector = new DStatsCollector();
    for (Metric metric : coreJvmMetrics.values()) {
      metric.collect(collector);
    }
    return collector.getList();
  }

  /**
   * Compare Metrics by name for sorting purposes.
   */
  protected static class NameComp implements Comparator<TimingMetricInfo> {

    @Override
    public int compare(TimingMetricInfo o1, TimingMetricInfo o2) {
      return o1.getName().compareTo(o2.getName());
    }

  }
}
