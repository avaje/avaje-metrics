package io.avaje.metrics.prometheus;

import io.avaje.metrics.Counter;
import io.avaje.metrics.GaugeDouble;
import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.Meter;
import io.avaje.metrics.Metric;
import io.avaje.metrics.Tags;
import io.avaje.metrics.Timer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class PrometheusWriter implements Metric.Visitor {

  private static final double MICROS_TO_SECONDS = 1_000_000D;
  private static final double MILLIS_TO_SECONDS = 1_000D;
  private static final String COUNTER = "counter";
  private static final String GAUGE = "gauge";
  private static final String HISTOGRAM = "histogram";
  private static final String SUMMARY = "summary";
  private static final String LE_LABEL = "le";

  private final Appendable out;
  private final long timedThresholdMicros;
  private final boolean includeMax;
  private final PrometheusNameCache nameCache;
  private final Set<String> typedMetrics = new HashSet<>();
  private final Map<MetricKey, Histogram> histograms = new LinkedHashMap<>();

  PrometheusWriter(Appendable out, long timedThresholdMicros, boolean includeMax, PrometheusNameCache nameCache) {
    this.out = out;
    this.timedThresholdMicros = timedThresholdMicros;
    this.includeMax = includeMax;
    this.nameCache = nameCache;
  }

  void write(List<Metric.Statistics> statistics) {
    try {
      for (var statistic : statistics) {
        statistic.visit(this);
      }
      writeHistograms();
    } catch (IOException e) {
      throw writeFailure(e);
    }
  }

  @Override
  public void visit(Timer.Stats timed) {
    try {
      if (timed.bucketRange() != null) {
        histogramFor(timed).add(timed);
        return;
      }
      writeTimer(timed);
    } catch (IOException e) {
      throw writeFailure(e);
    }
  }

  @Override
  public void visit(Meter.Stats stats) {
    try {
      var names = nameCache.meter(stats.name());
      writeType(names.count(), COUNTER);
      writeSample(names.count(), stats.id().tags(), stats.count());
      writeType(names.total(), COUNTER);
      writeSample(names.total(), stats.id().tags(), stats.total());
      if (includeMax) {
        writeGauge(names.max(), stats.id().tags(), stats.max());
      }
    } catch (IOException e) {
      throw writeFailure(e);
    }
  }

  @Override
  public void visit(Counter.Stats counter) {
    try {
      var name = nameCache.counter(counter.name());
      writeType(name, COUNTER);
      writeSample(name, counter.id().tags(), counter.count());
    } catch (IOException e) {
      throw writeFailure(e);
    }
  }

  @Override
  public void visit(GaugeDouble.Stats gauge) {
    try {
      writeGauge(nameCache.gauge(gauge.name()), gauge.id().tags(), Double.toString(gauge.value()));
    } catch (IOException e) {
      throw writeFailure(e);
    }
  }

  @Override
  public void visit(GaugeLong.Stats gauge) {
    try {
      writeGauge(nameCache.gauge(gauge.name()), gauge.id().tags(), gauge.value());
    } catch (IOException e) {
      throw writeFailure(e);
    }
  }

  private void writeTimer(Timer.Stats timed) throws IOException {
    if (belowThreshold(timed.total())) {
      return;
    }
    var names = nameCache.timer(timed.name());
    writeType(names.base(), SUMMARY);
    writeSample(names.count(), timed.id().tags(), timed.count());
    writeSample(names.sum(), timed.id().tags(), seconds(timed.total()));
    if (includeMax) {
      writeGauge(names.max(), timed.id().tags(), Double.toString(seconds(timed.max())));
    }
  }

  private void writeHistograms() throws IOException {
    for (var histogram : histograms.values()) {
      if (belowThreshold(histogram.total())) {
        continue;
      }
      histogram.writeTo(this);
    }
  }

  private boolean belowThreshold(long total) {
    return timedThresholdMicros > 0 && total < timedThresholdMicros;
  }

  private Histogram histogramFor(Timer.Stats timed) {
    var key = new MetricKey(timed.name(), timed.id().tags());
    return histograms.computeIfAbsent(key, Histogram::new);
  }

  private void writeGauge(String name, Tags tags, long value) throws IOException {
    writeGauge(name, tags, Long.toString(value));
  }

  private void writeGauge(String name, Tags tags, String value) throws IOException {
    writeType(name, GAUGE);
    writeSample(name, tags, value);
  }

  private void writeType(String name, String type) throws IOException {
    if (typedMetrics.add(name)) {
      out.append("# TYPE ").append(name).append(' ').append(type).append('\n');
    }
  }

  private void writeSample(String name, Tags tags, long value) throws IOException {
    writeSample(name, tags, Long.toString(value));
  }

  private void writeSample(String name, Tags tags, double value) throws IOException {
    writeSample(name, tags, Double.toString(value));
  }

  private void writeSample(String name, Tags tags, String value) throws IOException {
    out.append(name);
    appendLabels(tags);
    out.append(' ').append(value).append('\n');
  }

  private void writeSample(String name, Tags tags, String extraLabelName, String extraLabelValue, String value)
    throws IOException {

    out.append(name);
    appendLabels(tags, extraLabelName, extraLabelValue);
    out.append(' ').append(value).append('\n');
  }

  private void appendLabels(Tags tags) throws IOException {
    out.append(nameCache.labels(tags));
  }

  private void appendLabels(Tags tags, String extraLabelName, String extraLabelValue) throws IOException {
    out.append(nameCache.labels(tags, extraLabelName, extraLabelValue));
  }

  private static double seconds(long micros) {
    return micros / MICROS_TO_SECONDS;
  }

  private static String bucketUpperBound(String bucketRange) {
    var dash = bucketRange.indexOf('-');
    if (dash > 0) {
      return secondsFromMillis(bucketRange.substring(dash + 1));
    }
    return "+Inf";
  }

  private static String secondsFromMillis(String value) {
    return Double.toString(Long.parseLong(value) / MILLIS_TO_SECONDS);
  }

  private static UncheckedIOException writeFailure(IOException e) {
    return new UncheckedIOException("Error writing Prometheus metrics", e);
  }

  private static final class MetricKey {

    private final String name;
    private final Tags tags;
    private final String key;

    private MetricKey(String name, Tags tags) {
      this.name = name;
      this.tags = tags;
      this.key = name + '\n' + tags.cacheKey();
    }

    private String name() {
      return name;
    }

    private Tags tags() {
      return tags;
    }

    @Override
    public boolean equals(Object object) {
      if (this == object) {
        return true;
      }
      if (!(object instanceof MetricKey)) {
        return false;
      }
      return key.equals(((MetricKey) object).key);
    }

    @Override
    public int hashCode() {
      return key.hashCode();
    }
  }

  private static final class Histogram {

    private final MetricKey key;
    private final List<Bucket> buckets = new ArrayList<>();
    private long total;
    private long count;
    private long max;

    private Histogram(MetricKey key) {
      this.key = key;
    }

    private void add(Timer.Stats stats) {
      buckets.add(new Bucket(bucketUpperBound(stats.bucketRange()), stats.count()));
      total += stats.total();
      count += stats.count();
      if (stats.max() > max) {
        max = stats.max();
      }
    }

    private long total() {
      return total;
    }

    private void writeTo(PrometheusWriter writer) throws IOException {
      var names = writer.nameCache.timer(key.name());
      writer.writeType(names.base(), HISTOGRAM);
      long cumulativeCount = 0;
      for (var bucket : buckets) {
        cumulativeCount += bucket.count();
        writer.writeSample(
          names.bucket(),
          key.tags(),
          LE_LABEL,
          bucket.upperBound(),
          Long.toString(cumulativeCount));
      }
      writer.writeSample(names.count(), key.tags(), count);
      writer.writeSample(names.sum(), key.tags(), seconds(total));
      if (writer.includeMax) {
        writer.writeGauge(names.max(), key.tags(), Double.toString(seconds(max)));
      }
    }
  }

  private static final class Bucket {

    private final String upperBound;
    private final long count;

    private Bucket(String upperBound, long count) {
      this.upperBound = upperBound;
      this.count = count;
    }

    private String upperBound() {
      return upperBound;
    }

    private long count() {
      return count;
    }
  }
}
