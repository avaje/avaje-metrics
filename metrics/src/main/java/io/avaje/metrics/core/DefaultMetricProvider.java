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

  private final ConcurrentHashMap<String, Metric> metricsCache = new ConcurrentHashMap<>();
  private final SpiMetricBuilder.Factory<Timer> bucketTimerFactory;
  private final SpiMetricBuilder.Factory<Timer> timerFactory;
  private final SpiMetricBuilder.Factory<Counter> counterFactory;
  private final SpiMetricBuilder.Factory<Meter> meterFactory;
  private final List<MetricSupplier> suppliers = new ArrayList<>();
  private final Object monitor = new Object();
  private boolean withDetails;
  private boolean reportChangesOnly;
  private Function<String, String> namingConvention = NamingMatch.INSTANCE;

  public DefaultMetricProvider() {
    SpiMetricBuilder builder = initBuilder();
    this.bucketTimerFactory = builder.bucket();
    this.timerFactory = builder.timer();
    this.meterFactory = builder.meter();
    this.counterFactory = builder.counter();
  }

  DefaultMetricProvider(DefaultMetricProvider parent) {
    this.bucketTimerFactory = parent.bucketTimerFactory;
    this.timerFactory = parent.timerFactory;
    this.meterFactory = parent.meterFactory;
    this.counterFactory = parent.counterFactory;
    this.namingConvention = parent.namingConvention;
    this.withDetails = parent.withDetails;
    this.reportChangesOnly = parent.reportChangesOnly;
  }

  static SpiMetricBuilder initBuilder() {
    if (isDisableCollection()) {
      return ServiceLoader.load(SpiMetricBuilder.class)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Missing metrics-noop dependency"));
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
    JvmProcessMemory.createGauges(this, reportChangesOnly);
    return this;
  }

  @Override
  public JvmMetrics registerCGroupMetrics() {
    JvmCGroupCpu.createGauges(this, reportChangesOnly, withDetails);
    JvmCGroupMemory.createGauges(this, reportChangesOnly);
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
    JvmGarbageCollection.createGauges(this, withDetails);
    return this;
  }

  @Override
  public JvmMetrics registerJvmMemoryMetrics() {
    JvmMemory.createHeapGroup(this, reportChangesOnly, withDetails);
    JvmMemory.createNonHeapGroup(this, reportChangesOnly, withDetails);
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
    return (Timer) metric(name, timerFactory);
  }

  @Override
  public Timer timer(String name, int... bucketRanges) {
    return (Timer) metric(name, bucketTimerFactory, bucketRanges);
  }

  @Override
  public Counter counter(String name) {
    return (Counter) metric(name, counterFactory);
  }

  @Override
  public Meter meter(String name) {
    return (Meter) metric(name, meterFactory);
  }

  @Override
  public GaugeDouble gauge(String name, DoubleSupplier supplier) {
    return put(name, new DGaugeDouble(name, supplier));
  }

  @Override
  public GaugeLong gauge(String name, LongSupplier gauge) {
    return put(name, DGaugeLong.of(name, gauge));
  }

  private <T extends Metric> T put(String name, T metric) {
    metricsCache.put(name, metric);
    return metric;
  }

  private Metric metric(String name, SpiMetricBuilder.Factory<?> factory) {
    return metric(name, factory, null);
  }

  private Metric metric(String name, SpiMetricBuilder.Factory<?> factory, int[] bucketRanges) {
    // try lock free get first
    Metric metric = metricsCache.get(name);
    if (metric == null) {
      synchronized (monitor) {
        // use synchronized block
        metric = metricsCache.get(name);
        if (metric == null) {
          metric = factory.createMetric(name, bucketRanges);
          metricsCache.put(name, metric);
        }
      }
    }
    return metric;
  }

  @Override
  public void register(Metric metric) {
    metricsCache.put(metric.name(), metric);
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
