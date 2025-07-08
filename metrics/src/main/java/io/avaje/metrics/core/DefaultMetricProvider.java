package io.avaje.metrics.core;

import io.avaje.metrics.*;
import io.avaje.metrics.spi.SpiMetricBuilder;
import io.avaje.metrics.spi.SpiMetricProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.LongSupplier;

/**
 * Default implementation of the SpiMetricProvider.
 */
public final class DefaultMetricProvider implements SpiMetricProvider {

  private final ConcurrentHashMap<Metric.ID, Metric> metricsCache = new ConcurrentHashMap<>();
  private final SpiMetricBuilder.Factory<Timer> bucketTimerFactory;
  private final SpiMetricBuilder.Factory<Timer> timerFactory;
  private final SpiMetricBuilder.Factory<Counter> counterFactory;
  private final SpiMetricBuilder.Factory<Meter> meterFactory;
  private final List<MetricSupplier> suppliers = new ArrayList<>();
  private final Object monitor = new Object();
  private Tags globalTags;
  private boolean withDetails;
  private boolean reportChangesOnly;
  private Function<String, String> namingConvention = NamingMatch.INSTANCE;

  public DefaultMetricProvider() {
    SpiMetricBuilder builder = initBuilder();
    this.bucketTimerFactory = builder.bucket();
    this.timerFactory = builder.timer();
    this.meterFactory = builder.meter();
    this.counterFactory = builder.counter();
    this.globalTags = initGlobalTags();
  }

  DefaultMetricProvider(DefaultMetricProvider parent) {
    this.bucketTimerFactory = parent.bucketTimerFactory;
    this.timerFactory = parent.timerFactory;
    this.meterFactory = parent.meterFactory;
    this.counterFactory = parent.counterFactory;
    this.namingConvention = parent.namingConvention;
    this.withDetails = parent.withDetails;
    this.reportChangesOnly = parent.reportChangesOnly;
    this.globalTags = initGlobalTags();
  }

  static SpiMetricBuilder initBuilder() {
    if (isDisableCollection()) {
      return ServiceLoader.load(SpiMetricBuilder.class)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Missing metrics-noop dependency"));
    }
    return new DSpiMetricBuilder();
  }

  private static Tags initGlobalTags() {
    String hostname = System.getenv("HOSTNAME");
    if (hostname == null || hostname.trim().isEmpty()) {
      return Tags.EMPTY;
    } else {
      return Tags.of("pod:" + hostname.trim());
    }
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
  public String toString() {
    return String.valueOf(metricsCache.values());
  }

  Collection<Metric> metrics() {
    return metricsCache.values();
  }

  @Override
  public MetricRegistry createRegistry() {
    return new DefaultMetricProvider(this);
  }

  @Override
  public MetricRegistry naming(Function<String, String> namingConvention) {
    this.namingConvention = namingConvention;
    return this;
  }

  @Override
  public MetricRegistry namingUnderscore() {
    this.namingConvention = new UnderscoreNaming();
    return this;
  }

  @Override
  public JvmMetrics withGlobalTags(Tags globalTags) {
    this.globalTags = globalTags;
    return this;
  }

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
    registerJvmThreadMetrics();
    registerJvmOsLoadMetric();
    return this;
  }

  @Override
  public JvmMetrics registerProcessMemoryMetrics() {
    JvmProcessMemory.createGauges(this, reportChangesOnly, globalTags);
    return this;
  }

  @Override
  public JvmMetrics registerCGroupMetrics() {
    JvmCGroupCpu.createGauges(this, reportChangesOnly, withDetails, globalTags);
    JvmCGroupMemory.createGauges(this, reportChangesOnly, globalTags);
    return this;
  }

  @Override
  public JvmMetrics registerJvmOsLoadMetric() {
    GaugeLong osLoadAvgMetric = JvmOsLoad.osLoadAverage();
    if (osLoadAvgMetric.value() >= 0) {
      // OS Load Average is supported on this system
      register(osLoadAvgMetric);
    }
    return this;
  }

  @Override
  public JvmMetrics registerJvmThreadMetrics() {
    JvmThreads.createThreadMetricGroup(this, reportChangesOnly, withDetails);
    return this;
  }

  @Override
  public JvmMetrics registerJvmGCMetrics() {
    JvmGarbageCollection.createGauges(this, withDetails, globalTags);
    JvmGCPause.createMeters(this);
    return this;
  }

  @Override
  public JvmMetrics registerJvmMemoryMetrics() {
    JvmMemory.createHeapGroup(this, reportChangesOnly, withDetails, globalTags);
    JvmMemory.createNonHeapGroup(this, reportChangesOnly, withDetails, globalTags);
    return this;
  }

  @Override
  public void addSupplier(MetricSupplier supplier) {
    suppliers.add(supplier);
  }

  @Override
  public TimerGroup timerGroup(String baseName) {
    return new DTimerGroup(baseName, this);
  }

  @Override
  public Timer timer(String name) {
    return metric(Metric.ID.of(name), timerFactory);
  }

  @Override
  public Timer timer(String name, Tags tags) {
    return metric(Metric.ID.of(name, tags), timerFactory);
  }

  @Override
  public Timer timer(String name, int... bucketRanges) {
    return metric(Metric.ID.of(name), bucketTimerFactory, bucketRanges);
  }

  @Override
  public Timer timer(String name, Tags tags, int... bucketRanges) {
    return metric(Metric.ID.of(name, tags), bucketTimerFactory, bucketRanges);
  }

  @Override
  public Counter counter(String name) {
    return metric(Metric.ID.of(name), counterFactory);
  }

  @Override
  public Counter counter(String name, Tags tags) {
    return metric(Metric.ID.of(name, tags), counterFactory);
  }

  @Override
  public Meter meter(String name) {
    return metric(Metric.ID.of(name), meterFactory);
  }

  @Override
  public Meter meter(String name, Tags tags) {
    return metric(Metric.ID.of(name, tags), meterFactory);
  }

  @Override
  public GaugeDouble gauge(String name, DoubleSupplier supplier) {
    return put(new DGaugeDouble(Metric.ID.of(name), supplier));
  }

  @Override
  public GaugeDouble gauge(String name, Tags tags, DoubleSupplier supplier) {
    return put(new DGaugeDouble(Metric.ID.of(name, tags), supplier));
  }

  @Override
  public GaugeLong gauge(String name, LongSupplier gauge) {
    return put(DGaugeLong.of(Metric.ID.of(name), gauge));
  }

  @Override
  public GaugeLong gauge(String name, Tags tags, LongSupplier gauge) {
    return put(DGaugeLong.of(Metric.ID.of(name, tags), gauge));
  }

  private <T extends Metric> T put(T metric) {
    metricsCache.put(metric.id(), metric);
    return metric;
  }

  private <T extends Metric> T metric(Metric.ID id, SpiMetricBuilder.Factory<?> factory) {
    return metric(id, factory, null);
  }

  @SuppressWarnings("unchecked")
  private <T extends Metric> T metric(Metric.ID id, SpiMetricBuilder.Factory<?> factory, int[] bucketRanges) {
    // try lock free get first
    Metric metric = metricsCache.get(id);
    if (metric == null) {
      synchronized (monitor) {
        // use synchronized block
        metric = metricsCache.get(id);
        if (metric == null) {
          metric = factory.createMetric(id, bucketRanges);
          metricsCache.put(id, metric);
        }
      }
    }
    return (T) metric;
  }

  @Override
  public void register(Metric metric) {
    metricsCache.put(metric.id(), metric);
  }

  @Override
  public List<Metric.Statistics> collectMetrics() {
    synchronized (monitor) {
      final DStatsCollector collector = new DStatsCollector(namingConvention);
      collectAppMetrics(collector);
      return collector.list();
    }
  }

  @Override
  public JsonMetrics collectAsJson() {
    return new DJson(this);
  }

  private void collectAppMetrics(DStatsCollector collector) {
    for (Metric metric : metricsCache.values()) {
      metric.collect(collector);
    }
    for (MetricSupplier supplier : suppliers) {
      collector.addAll(supplier.collectMetrics());
    }
  }

  private static class DJson implements JsonMetrics {

    private final DefaultMetricProvider provider;

    DJson(DefaultMetricProvider provider) {
      this.provider = provider;
    }

    @Override
    public void write(Appendable appendable) {
      JsonWriter.writeTo(appendable, provider.collectMetrics());
    }

    @Override
    public String asJson() {
      final var buffer = new StringBuilder(1000);
      buffer.append("[\n");
      write(buffer);
      buffer.append("]");
      return buffer.toString();
    }
  }
}
