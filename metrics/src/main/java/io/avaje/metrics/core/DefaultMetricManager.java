package io.avaje.metrics.core;

import io.avaje.metrics.*;
import io.avaje.metrics.spi.SpiMetricBuilder;
import io.avaje.metrics.spi.SpiMetricManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of the PluginMetricManager.
 */
public class DefaultMetricManager implements SpiMetricManager {

  private static final String METRICS_MDC_REQUEST_ID = "metrics.mdc.requestId";
  private static final String JVM = "jvm.";

//  private final NameComp sortByName = new NameComp();
  private final Object monitor = new Object();
  private boolean withDetails;
  private boolean reportChangesOnly;

  private final List<Metric> coreJvmMetrics = new ArrayList<>();
  private final ConcurrentHashMap<String, Metric> metricsCache = new ConcurrentHashMap<>();
  private final SpiMetricBuilder.Factory<TimedMetric> bucketTimedMetricFactory;
  private final SpiMetricBuilder.Factory<TimedMetric> timedMetricFactory;
  private final SpiMetricBuilder.Factory<CounterMetric> counterMetricFactory;
  private final SpiMetricBuilder.Factory<ValueMetric> valueMetricFactory;

  private final ConcurrentHashMap<String, MetricNameCache> nameCache = new ConcurrentHashMap<>();
//  private final ConcurrentLinkedQueue<RequestTiming> requestTimings = new ConcurrentLinkedQueue<>();
  private final List<MetricSupplier> suppliers = new ArrayList<>();

//  /**
//   * Adapter that can obtain a request id to associate with request timings.
//   */
//  protected final ExternalRequestIdAdapter externalRequestIdAdapter;

  public DefaultMetricManager() {
    SpiMetricBuilder builder = initBuilder();
    this.bucketTimedMetricFactory = builder.bucket();
    this.timedMetricFactory = builder.timed();
    this.valueMetricFactory = builder.value();
    this.counterMetricFactory = builder.counter();
//    this.externalRequestIdAdapter = initExternalRequestIdAdapter(isDisableCollection());
  }

//  private static ExternalRequestIdAdapter initExternalRequestIdAdapter(boolean disable) {
//    if (disable) {
//      return null;
//    }
//    String mdcKey = System.getProperty(METRICS_MDC_REQUEST_ID);
//    if (mdcKey != null) {
//      return new MdcExternalRequestIdAdapter(mdcKey);
//    }
//    return null;
//  }

  static SpiMetricBuilder initBuilder() {
    if (isDisableCollection()) {
      return ServiceLoader.load(SpiMetricBuilder.class).findFirst().orElseThrow(() -> new IllegalStateException("Missing metrics-noop dependency"));
    }
    return new DSpiMetricBuilder();
  }

  /**
   * Return true if metric collection should be disabled.
   * This has the effect that NOOP metric implementations are used.
   */
  private static boolean isDisableCollection() {
    String disable = System.getProperty("metrics.disable", System.getenv("METRICS_DISABLE"));
    return "true".equalsIgnoreCase(disable);
  }

//  @Override
//  public void reportTiming(RequestTiming requestTiming) {
//    if (externalRequestIdAdapter != null) {
//      String requestId = externalRequestIdAdapter.getExternalRequestId();
//      if (requestId != null) {
//        requestTiming.setExternalRequestId(requestId);
//      }
//    }
//    requestTimings.add(requestTiming);
//  }

  @Override
  public JvmMetrics withReportChangesOnly() {
    reportChangesOnly = true;
    return this;
  }

  @Override
  public JvmMetrics withReportAlways() {
    reportChangesOnly = false;
    return this;
  }

  @Override
  public JvmMetrics withDetails() {
    withDetails = true;
    return this;
  }

  /**
   * Register the standard JVM metrics.
   */
  @Override
  public JvmMetrics registerJvmMetrics() {
    registerJvmGCMetrics();
    registerJvmMemoryMetrics();
    registerProcessMemoryMetrics();
    registerJvmThreadMetrics();
    registerJvmOsLoadMetric();
    return this;
  }

  @Override
  public JvmMetrics registerProcessMemoryMetrics() {
    registerAll(JvmProcessMemory.createGauges(reportChangesOnly));
    return this;
  }

  @Override
  public JvmMetrics registerCGroupMetrics() {
    registerAll(JvmCGroupCpuMetricGroup.createGauges(reportChangesOnly));
    registerAll(JvmCGroupMemoryMetricGroup.createGauges(reportChangesOnly));
    return this;
  }

  @Override
  public JvmMetrics registerJvmOsLoadMetric() {
    GaugeLongMetric osLoadAvgMetric = JvmSystemMetricGroup.getOsLoadAvgMetric();
    if (osLoadAvgMetric.value() >= 0) {
      // OS Load Average is supported on this system
      registerJvmMetric(osLoadAvgMetric);
    }
    return this;
  }

  @Override
  public JvmMetrics registerJvmThreadMetrics() {
    registerAll(JvmThreadMetricGroup.createThreadMetricGroup(reportChangesOnly, withDetails));
    return this;
  }

  @Override
  public JvmMetrics registerJvmGCMetrics() {
    registerAll(JvmGarbageCollectionMetricGroup.createGauges(withDetails));
    return this;
  }

  @Override
  public JvmMetrics registerJvmMemoryMetrics() {
    registerAll(JvmMemoryMetricGroup.createHeapGroup(reportChangesOnly));
    registerAll(JvmMemoryMetricGroup.createNonHeapGroup(reportChangesOnly));
    return this;
  }

  private void registerAll(List<Metric> groups) {
    for (Metric metric : groups) {
      registerJvmMetric(metric);
    }
  }

  private void registerJvmMetric(Metric m) {
    synchronized (monitor) {
      coreJvmMetrics.add(m);
    }
  }

  @Override
  public void addSupplier(MetricSupplier supplier) {
    suppliers.add(supplier);
  }

  @Override
  public MetricName name(String name) {
    return new DMetricName(name);
  }

  @Override
  public MetricName name(Class<?> cls, String name) {
    return new DMetricName(cls, name);
  }

  @Override
  public MetricNameCache nameCache(Class<?> klass) {
    return nameCache(name(klass, null));
  }

  @Override
  public MetricNameCache nameCache(MetricName baseName) {
    String key = baseName.simpleName();
    MetricNameCache metricNameCache = nameCache.get(key);
    if (metricNameCache == null) {
      metricNameCache = new DMetricNameCache(baseName);
      MetricNameCache oldNameCache = nameCache.putIfAbsent(key, metricNameCache);
      if (oldNameCache != null) {
        return oldNameCache;
      }
    }
    return metricNameCache;
  }

  @Override
  public TimedMetricGroup timedGroup(MetricName baseName) {
    return new DTimedMetricGroup(baseName);
  }

  @Override
  public TimedMetric timed(MetricName name) {
    return (TimedMetric) getMetric(name, timedMetricFactory);
  }

  @Override
  public TimedMetric timed(MetricName name, int... bucketRanges) {
    return (TimedMetric) getMetric(name, bucketTimedMetricFactory, bucketRanges);
  }

  @Override
  public CounterMetric counter(MetricName name) {
    return (CounterMetric) getMetric(name, counterMetricFactory);
  }

  @Override
  public ValueMetric value(MetricName name) {
    return (ValueMetric) getMetric(name, valueMetricFactory);
  }

  @Override
  public GaugeDoubleMetric register(MetricName name, GaugeDouble gauge) {
    return put(name, new DGaugeDoubleMetric(name, gauge));
  }

  @Override
  public GaugeLongMetric register(MetricName name, GaugeLong gauge) {
    return put(name, (GaugeLongMetric) new DGaugeLongMetric(name, gauge));
  }

  private <T extends Metric> T put(MetricName name, T metric) {
    if (name.startsWith(JVM)) {
      registerJvmMetric(metric);
    } else {
      metricsCache.put(name.simpleName(), metric);
    }
    return metric;
  }

  private Metric getMetric(MetricName name, SpiMetricBuilder.Factory<?> factory) {
    return getMetric(name, factory, null);
  }

  private Metric getMetric(MetricName name, SpiMetricBuilder.Factory<?> factory, int[] bucketRanges) {
    String cacheKey = name.simpleName();
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
    return metricsCache.get(name.simpleName());
  }

//  @Override
//  public boolean setRequestTimingCollection(String fullMetricName, int collectionCount) {
//    return setRequestTimingCollection(MetricName.of(fullMetricName), collectionCount);
//  }
//
//  @Override
//  public boolean setRequestTimingCollection(Class<?> cls, String name, int collectionCount) {
//    return setRequestTimingCollection(MetricName.of(cls, name), collectionCount);
//  }
//
//  /**
//   * Set request timing collection on a specific timed metric.
//   * Return false if the metric was not found (and timing collection was not set).
//   */
//  protected boolean setRequestTimingCollection(MetricName metricName, int collectionCount) {
//    Metric metric = getMetricWithoutCreate(metricName);
//    if (metric instanceof TimedMetric) {
//      TimedMetric timed = (TimedMetric) metric;
//      timed.setRequestTiming(collectionCount);
//      return true;
//    }
//    return false;
//  }
//
//  /**
//   * Set request timing collection on metrics that match the nameMatch expression.
//   *
//   * @param nameMatch       The expression used to match timing metrics
//   * @param collectionCount The number of requests to collect
//   * @return The timing metrics that had the request timing collection set
//   */
//  @Override
//  public List<TimingMetricInfo> setRequestTimingCollectionUsingMatch(String nameMatch, int collectionCount) {
//    if (nameMatch == null || nameMatch.trim().length() == 0) {
//      // not turning on collection globally for empty
//      return Collections.emptyList();
//    }
//
//    LikeMatcher like = new LikeMatcher(nameMatch);
//    List<TimingMetricInfo> changes = new ArrayList<>();
//
//    for (Metric metric : metricsCache.values()) {
//      if (metric instanceof TimedMetric) {
//        TimedMetric timed = (TimedMetric) metric;
//        if (like.matches(timed.name().simpleName())) {
//          timed.setRequestTiming(collectionCount);
//          changes.add(new TimingMetricInfo(timed.name().simpleName(), collectionCount));
//        }
//      }
//    }
//    // ensure we return them in a predicable order
//    Collections.sort(changes, sortByName);
//    return changes;
//  }
//
//  /**
//   * Return currently active timing metrics that match the name expression.
//   */
//  @Override
//  public List<TimingMetricInfo> getRequestTimingMetrics(String nameMatchExpression) {
//    return getRequestTimingMetrics(true, nameMatchExpression);
//  }
//
//  /**
//   * Return the list of all timing metrics that match the name expression.
//   */
//  @Override
//  public List<TimingMetricInfo> getAllTimingMetrics(String nameMatchExpression) {
//    return getRequestTimingMetrics(false, nameMatchExpression);
//  }
//
//  /**
//   * Return the list of timing metrics that we are interested in.
//   *
//   * @param activeOnly          true if we only want timing metrics that are actively collecting request timings.
//   * @param nameMatchExpression the expression used to match/filter metric names. Null or empty means match all.
//   */
//  protected List<TimingMetricInfo> getRequestTimingMetrics(boolean activeOnly, String nameMatchExpression) {
//    synchronized (monitor) {
//      List<TimingMetricInfo> list = new ArrayList<>();
//      LikeMatcher like = new LikeMatcher(nameMatchExpression);
//
//      for (Metric metric : metricsCache.values()) {
//        if (metric instanceof TimedMetric) {
//          TimedMetric timed = (TimedMetric) metric;
//          if (!activeOnly || timed.getRequestTiming() >= 1) {
//            // actively collection or doing the all search
//            if (like.matches(timed.name().simpleName())) {
//              // metric name matches our expression
//              list.add(new TimingMetricInfo(timed.name().simpleName(), timed.getRequestTiming()));
//            }
//          }
//        }
//      }
//      // ensure we return them in a predicable order
//      Collections.sort(list, sortByName);
//      return list;
//    }
//  }

//  @Override
//  public Collection<Metric> getJvmMetrics() {
//    return Collections.unmodifiableList(coreJvmMetrics);
//  }

//  /**
//   * Return the request timings that have been collected since the last collection.
//   */
//  public List<RequestTiming> collectRequestTimings() {
//    List<RequestTiming> list = new ArrayList<>();
//    RequestTiming req;
//    while ((req = requestTimings.poll()) != null) {
//      list.add(req);
//    }
//    return list;
//  }

  private void collectJvmMetrics(DStatsCollector collector) {
    for (Metric metric : coreJvmMetrics) {
      metric.collect(collector);
    }
  }

  private void collectAppMetrics(DStatsCollector collector) {
    for (Metric metric : metricsCache.values()) {
      metric.collect(collector);
    }
    for (MetricSupplier supplier : suppliers) {
      collector.addAll(supplier.collectMetrics());
    }
  }


  @Override
  public List<MetricStats> collectMetrics() {
    synchronized (monitor) {
      DStatsCollector collector = new DStatsCollector();
      collectJvmMetrics(collector);
      collectAppMetrics(collector);
      return collector.getList();
    }
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
