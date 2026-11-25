package io.avaje.metrics.otel.producer;

import io.avaje.metrics.Counter;
import io.avaje.metrics.GaugeDouble;
import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.Meter;
import io.avaje.metrics.Metric;
import io.avaje.metrics.Tags;
import io.avaje.metrics.Timer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.GaugeData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.SumData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.resources.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class MetricDataMapper {

  private static final String DESCRIPTION = "";
  private static final String COUNT_UNIT = "{event}";

  private final InstrumentationScopeInfo scopeInfo;
  private final long timedThresholdMicros;
  private final ConcurrentHashMap<String, Attributes> attributeCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, DerivedMetricNames> derivedNameCache = new ConcurrentHashMap<>();

  MetricDataMapper(InstrumentationScopeInfo scopeInfo, long timedThresholdMicros) {
    this.scopeInfo = scopeInfo;
    this.timedThresholdMicros = timedThresholdMicros;
  }

  Collection<MetricData> map(
    Resource resource,
    long startEpochNanos,
    long epochNanos,
    Collection<Metric.Statistics> statistics) {

    if (statistics.isEmpty()) {
      return List.of();
    }

    var visitor = new ProduceVisitor(resource, startEpochNanos, epochNanos, statistics.size() * 3);
    for (var statistic : statistics) {
      statistic.visit(visitor);
    }
    return visitor.metrics();
  }

  private final class ProduceVisitor implements Metric.Visitor {

    private final Resource resource;
    private final long startEpochNanos;
    private final long epochNanos;
    private final Map<String, MetricSeries> series;

    private ProduceVisitor(Resource resource, long startEpochNanos, long epochNanos, int capacity) {
      this.resource = resource;
      this.startEpochNanos = startEpochNanos;
      this.epochNanos = epochNanos;
      this.series = new LinkedHashMap<>(capacity);
    }

    @Override
    public void visit(Timer.Stats stats) {
      if (timedThresholdMicros > 0 && stats.total() < timedThresholdMicros) {
        return;
      }
      if (stats.count() == 0) {
        return;
      }
      recordMeterStats(stats, stats.unit());
    }

    @Override
    public void visit(Meter.Stats stats) {
      if (stats.count() == 0) {
        return;
      }
      recordMeterStats(stats, stats.unit());
    }

    @Override
    public void visit(Counter.Stats stats) {
      if (stats.count() == 0) {
        return;
      }
      addLongSum(stats.name(), stats.unit(), stats.count(), true, attributes(stats.id()));
    }

    @Override
    public void visit(GaugeDouble.Stats stats) {
      addDoubleGauge(stats.name(), stats.unit(), stats.value(), attributes(stats.id()));
    }

    @Override
    public void visit(GaugeLong.Stats stats) {
      addLongGauge(stats.name(), stats.unit(), stats.value(), attributes(stats.id()));
    }

    private List<MetricData> metrics() {
      var metrics = new ArrayList<MetricData>(series.size());
      for (var item : series.values()) {
        metrics.add(item.toMetricData(resource, scopeInfo));
      }
      return metrics;
    }

    private void recordMeterStats(Meter.Stats stats, String unit) {
      var names = derivedMetricNames(stats.name());
      var attributes = attributes(stats.id());
      addLongSum(names.count(), COUNT_UNIT, stats.count(), true, attributes);
      addLongSum(names.total(), unit, stats.total(), true, attributes);
      addLongGauge(names.max(), unit, stats.max(), attributes);
    }

    private void addLongSum(String name, String unit, long value, boolean monotonic, Attributes attributes) {
      var target = (LongSumSeries) series.computeIfAbsent("S:" + name, key -> new LongSumSeries(name, unit, monotonic));
      target.add(LongPointData.create(startEpochNanos, epochNanos, attributes, value));
    }

    private void addLongGauge(String name, String unit, long value, Attributes attributes) {
      var target = (LongGaugeSeries) series.computeIfAbsent("LG:" + name, key -> new LongGaugeSeries(name, unit));
      target.add(LongPointData.create(startEpochNanos, epochNanos, attributes, value));
    }

    private void addDoubleGauge(String name, String unit, double value, Attributes attributes) {
      var target = (DoubleGaugeSeries) series.computeIfAbsent("DG:" + name, key -> new DoubleGaugeSeries(name, unit));
      target.add(DoublePointData.create(startEpochNanos, epochNanos, attributes, value, List.of()));
    }
  }

  private interface MetricSeries {
    MetricData toMetricData(Resource resource, InstrumentationScopeInfo scopeInfo);
  }

  private static final class LongSumSeries implements MetricSeries {

    private final String name;
    private final String unit;
    private final boolean monotonic;
    private final List<LongPointData> points = new ArrayList<>(4);

    private LongSumSeries(String name, String unit, boolean monotonic) {
      this.name = name;
      this.unit = unit;
      this.monotonic = monotonic;
    }

    private void add(LongPointData point) {
      points.add(point);
    }

    @Override
    public MetricData toMetricData(Resource resource, InstrumentationScopeInfo scopeInfo) {
      return ImmutableMetricData.createLongSum(
        resource,
        scopeInfo,
        name,
        DESCRIPTION,
        unit,
        SumData.createLongSumData(monotonic, AggregationTemporality.CUMULATIVE, points));
    }
  }

  private static final class LongGaugeSeries implements MetricSeries {

    private final String name;
    private final String unit;
    private final List<LongPointData> points = new ArrayList<>(4);

    private LongGaugeSeries(String name, String unit) {
      this.name = name;
      this.unit = unit;
    }

    private void add(LongPointData point) {
      points.add(point);
    }

    @Override
    public MetricData toMetricData(Resource resource, InstrumentationScopeInfo scopeInfo) {
      return ImmutableMetricData.createLongGauge(
        resource,
        scopeInfo,
        name,
        DESCRIPTION,
        unit,
        GaugeData.createLongGaugeData(points));
    }
  }

  private static final class DoubleGaugeSeries implements MetricSeries {

    private final String name;
    private final String unit;
    private final List<DoublePointData> points = new ArrayList<>(4);

    private DoubleGaugeSeries(String name, String unit) {
      this.name = name;
      this.unit = unit;
    }

    private void add(DoublePointData point) {
      points.add(point);
    }

    @Override
    public MetricData toMetricData(Resource resource, InstrumentationScopeInfo scopeInfo) {
      return ImmutableMetricData.createDoubleGauge(
        resource,
        scopeInfo,
        name,
        DESCRIPTION,
        unit,
        GaugeData.createDoubleGaugeData(points));
    }
  }

  private Attributes attributes(Metric.ID id) {
    return attributes(id.tags());
  }

  private Attributes attributes(Tags tags) {
    var cacheKey = tags.cacheKey();
    if (cacheKey.isEmpty()) {
      return Attributes.empty();
    }
    return attributeCache.computeIfAbsent(cacheKey, key -> buildAttributes(tags));
  }

  private Attributes buildAttributes(Tags tags) {
    AttributesBuilder builder = Attributes.builder();
    for (String tag : tags.array()) {
      if (tag != null) {
        int colon = tag.indexOf(':');
        if (colon > 0) {
          builder.put(tag.substring(0, colon), tag.substring(colon + 1));
        }
      }
    }
    return builder.build();
  }

  private DerivedMetricNames derivedMetricNames(String name) {
    return derivedNameCache.computeIfAbsent(name, DerivedMetricNames::new);
  }

  private static final class DerivedMetricNames {

    private final String count;
    private final String total;
    private final String max;

    private DerivedMetricNames(String baseName) {
      this.count = baseName + ".count";
      this.total = baseName + ".total";
      this.max = baseName + ".max";
    }

    private String count() {
      return count;
    }

    private String total() {
      return total;
    }

    private String max() {
      return max;
    }
  }
}
