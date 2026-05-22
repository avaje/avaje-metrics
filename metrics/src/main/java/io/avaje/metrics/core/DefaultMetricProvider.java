package io.avaje.metrics.core;

import io.avaje.metrics.*;
import io.avaje.metrics.spi.SpiMetricBuilder;
import io.avaje.metrics.spi.SpiMetricProvider;
import io.avaje.metrics.spi.SpiTimedSpanFactory;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.LongSupplier;

import static java.util.Objects.requireNonNull;

/**
 * Default implementation of the SpiMetricProvider.
 */
public final class DefaultMetricProvider implements SpiMetricProvider {

  private static final String COUNT_UNIT = "{event}";
  private static final String DEFAULT_UNIT = "";
  private static final String TIMER_UNIT = "us";

  private final ConcurrentHashMap<Metric.ID, Metric> metricsCache = new ConcurrentHashMap<>();
  private final SpiMetricBuilder.Factory<Timer> bucketTimerFactory;
  private final SpiMetricBuilder.Factory<Timer> timerFactory;
  private final @Nullable SpiTimedSpanFactory timedSpanFactory;
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
    this.timedSpanFactory = initTimedSpanFactory();
    this.meterFactory = builder.meter();
    this.counterFactory = builder.counter();
    this.globalTags = initGlobalTags();
  }

  DefaultMetricProvider(DefaultMetricProvider parent) {
    this.bucketTimerFactory = parent.bucketTimerFactory;
    this.timerFactory = parent.timerFactory;
    this.timedSpanFactory = parent.timedSpanFactory;
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

  static @Nullable SpiTimedSpanFactory initTimedSpanFactory() {
    return ServiceLoader.load(SpiTimedSpanFactory.class)
      .findFirst()
      .orElse(null);
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
  public JvmMetrics registerJvmCoreMetrics() {
    JvmMemory.createHeapUsed(this, reportChangesOnly, globalTags);
    JvmProcessMemory.createGauges(this, reportChangesOnly, false, globalTags);
    JvmThreads.createThreadMetricGroup(this, reportChangesOnly, false);
    return this;
  }

  @Override
  public JvmMetrics registerProcessMemoryMetrics() {
    JvmProcessMemory.createGauges(this, reportChangesOnly, true, globalTags);
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
    return metric(Metric.ID.of(name), TIMER_UNIT, timerFactory, Timer.class, null);
  }

  @Override
  public Timer tracedTimer(String name) {
    return tracedMetric(Metric.ID.of(name), TIMER_UNIT, timerFactory, null);
  }

  @Override
  public Timer timer(String name, Tags tags) {
    return metric(Metric.ID.of(name, tags), TIMER_UNIT, timerFactory, Timer.class, null);
  }

  @Override
  public Timer tracedTimer(String name, Tags tags) {
    return tracedMetric(Metric.ID.of(name, tags), TIMER_UNIT, timerFactory, null);
  }

  @Override
  public Timer timer(String name, int... bucketRanges) {
    return metric(Metric.ID.of(name), TIMER_UNIT, bucketTimerFactory, Timer.class, bucketRanges);
  }

  @Override
  public Timer tracedTimer(String name, int... bucketRanges) {
    return tracedMetric(Metric.ID.of(name), TIMER_UNIT, bucketTimerFactory, bucketRanges);
  }

  @Override
  public Timer timer(String name, Tags tags, int... bucketRanges) {
    return metric(Metric.ID.of(name, tags), TIMER_UNIT, bucketTimerFactory, Timer.class, bucketRanges);
  }

  @Override
  public Timer tracedTimer(String name, Tags tags, int... bucketRanges) {
    return tracedMetric(Metric.ID.of(name, tags), TIMER_UNIT, bucketTimerFactory, bucketRanges);
  }

  @Override
  public Counter counter(String name) {
    return counterBuilder(name).build();
  }

  @Override
  public CounterBuilder counterBuilder(String name) {
    return new DCounterBuilder(name);
  }

  @Override
  public Meter meter(String name) {
    return meterBuilder(name).build();
  }

  @Override
  public MeterBuilder meterBuilder(String name) {
    return new DMeterBuilder(name);
  }

  @Override
  public GaugeBuilder gauge(String name) {
    return new DGaugeBuilder(name);
  }

  @Override
  public GaugeDouble gauge(String name, DoubleSupplier supplier) {
    return gaugeDouble(name, Tags.EMPTY, DEFAULT_UNIT, supplier);
  }

  @Override
  public GaugeLong gauge(String name, LongSupplier gauge) {
    return gaugeLong(name, Tags.EMPTY, DEFAULT_UNIT, gauge);
  }

  private <T extends Metric> T replace(T metric, Class<T> type) {
    synchronized (monitor) {
      var existing = metricsCache.get(metric.id());
      if (existing != null) {
        validateMetric(metric.id(), existing, type, metric.unit());
      }
      metricsCache.put(metric.id(), metric);
    }
    return metric;
  }

  private GaugeDouble gaugeDouble(String name, Tags tags, String unit, DoubleSupplier supplier) {
    return replace(
      new DGaugeDouble(Metric.ID.of(name, tags), unit, requireNonNull(supplier, "supplier")), GaugeDouble.class);
  }

  private GaugeLong gaugeLong(String name, Tags tags, String unit, LongSupplier supplier) {
    return replace(
      DGaugeLong.of(Metric.ID.of(name, tags), unit, requireNonNull(supplier, "supplier")), GaugeLong.class);
  }

  private Counter counter(String name, Tags tags, String unit) {
    return metric(Metric.ID.of(name, tags), unit, counterFactory, Counter.class, null);
  }

  private Meter meter(String name, Tags tags, String unit) {
    return metric(Metric.ID.of(name, tags), unit, meterFactory, Meter.class, null);
  }

  private <T extends Metric> T metric(
    Metric.ID id,
    String unit,
    SpiMetricBuilder.Factory<?> factory,
    Class<T> type,
    int[] bucketRanges) {

    var normalizedUnit = BaseReportName.normalizeUnit(unit);
    // try lock free get first
    Metric metric = metricsCache.get(id);
    if (metric == null) {
      synchronized (monitor) {
        // use synchronized block
        metric = metricsCache.get(id);
        if (metric == null) {
          metric = factory.createMetric(id, normalizedUnit, bucketRanges);
          metricsCache.put(id, metric);
        }
      }
    }
    return validateMetric(id, metric, type, normalizedUnit);
  }

  private Timer tracedMetric(Metric.ID id, String unit, SpiMetricBuilder.Factory<?> factory, int[] bucketRanges) {
    var normalizedUnit = BaseReportName.normalizeUnit(unit);
    Metric metric = metricsCache.get(id);
    if (metric == null) {
      synchronized (monitor) {
        metric = metricsCache.get(id);
        if (metric == null) {
          metric = factory.createMetric(id, normalizedUnit, bucketRanges);
          if (metric instanceof TraceableTimer) {
            metric = ((TraceableTimer) metric).withTracing(timedSpanFactory);
          }
          metricsCache.put(id, metric);
        }
      }
    }
    return validateMetric(id, metric, Timer.class, normalizedUnit);
  }

  @Override
  public void register(Metric metric) {
    synchronized (monitor) {
      var existing = metricsCache.get(metric.id());
      if (existing != null) {
        validateUnit(metric.id(), existing, metric.unit());
      }
      metricsCache.put(metric.id(), metric);
    }
  }

  private static void validateUnit(Metric.ID id, Metric existing, String unit) {
    var normalizedUnit = BaseReportName.normalizeUnit(unit);
    if (!existing.unit().equals(normalizedUnit)) {
      throw new IllegalStateException(
        "Metric " + id.name() + " already registered with unit '" + existing.unit() + "' not '" + normalizedUnit + "'");
    }
  }

  private static <T extends Metric> T validateMetric(Metric.ID id, Metric metric, Class<T> type, String unit) {
    if (!type.isInstance(metric)) {
      throw new IllegalStateException(
        "Metric " + id.name() + " already registered as " + metric.getClass().getSimpleName()
          + " not " + type.getSimpleName());
    }
    validateUnit(id, metric, unit);
    return type.cast(metric);
  }

  private final class DGaugeBuilder implements GaugeBuilder {

    private final String name;
    private Tags tags = Tags.EMPTY;
    private String unit = DEFAULT_UNIT;

    private DGaugeBuilder(String name) {
      this.name = requireNonNull(name, "name");
    }

    @Override
    public GaugeBuilder tags(Tags tags) {
      this.tags = requireNonNull(tags, "tags");
      return this;
    }

    @Override
    public GaugeBuilder unit(String unit) {
      this.unit = BaseReportName.normalizeUnit(unit);
      return this;
    }

    @Override
    public GaugeLong ofLongs(LongSupplier supplier) {
      return gaugeLong(name, tags, unit, supplier);
    }

    @Override
    public GaugeDouble ofDoubles(DoubleSupplier supplier) {
      return gaugeDouble(name, tags, unit, supplier);
    }
  }

  private final class DCounterBuilder implements CounterBuilder {

    private final String name;
    private Tags tags = Tags.EMPTY;
    private String unit = COUNT_UNIT;

    private DCounterBuilder(String name) {
      this.name = requireNonNull(name, "name");
    }

    @Override
    public CounterBuilder tags(Tags tags) {
      this.tags = requireNonNull(tags, "tags");
      return this;
    }

    @Override
    public CounterBuilder unit(String unit) {
      this.unit = BaseReportName.normalizeUnit(unit);
      return this;
    }

    @Override
    public Counter build() {
      return counter(name, tags, unit);
    }
  }

  private final class DMeterBuilder implements MeterBuilder {

    private final String name;
    private Tags tags = Tags.EMPTY;
    private String unit = DEFAULT_UNIT;

    private DMeterBuilder(String name) {
      this.name = requireNonNull(name, "name");
    }

    @Override
    public MeterBuilder tags(Tags tags) {
      this.tags = requireNonNull(tags, "tags");
      return this;
    }

    @Override
    public MeterBuilder unit(String unit) {
      this.unit = BaseReportName.normalizeUnit(unit);
      return this;
    }

    @Override
    public Meter build() {
      return meter(name, tags, unit);
    }
  }

  @Override
  public List<Metric.Statistics> collectMetrics() {
    return collectMetrics(CollectionMode.DELTA);
  }

  @Override
  public List<Metric.Statistics> collectMetrics(CollectionMode mode) {
    synchronized (monitor) {
      final DStatsCollector collector = new DStatsCollector(namingConvention, mode);
      collectAppMetrics(collector);
      return collector.list();
    }
  }

  @Override
  public JsonMetrics collectAsJson() {
    return collectAsJson(CollectionMode.DELTA);
  }

  @Override
  public JsonMetrics collectAsJson(CollectionMode mode) {
    return new DJson(this, mode);
  }

  private void collectAppMetrics(DStatsCollector collector) {
    for (Metric metric : metricsCache.values()) {
      metric.collect(collector);
    }
    for (MetricSupplier supplier : suppliers) {
      collector.addAll(supplier.collectMetrics(collector.collectionMode()));
    }
  }

  private static class DJson implements JsonMetrics {

    private final DefaultMetricProvider provider;
    private final CollectionMode mode;

    DJson(DefaultMetricProvider provider, CollectionMode mode) {
      this.provider = provider;
      this.mode = mode;
    }

    @Override
    public void write(Appendable appendable) {
      JsonWriter.writeTo(appendable, provider.collectMetrics(mode));
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
