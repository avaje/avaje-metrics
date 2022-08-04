package io.avaje.metrics.report;


import io.avaje.metrics.*;

import java.io.IOException;
import java.util.List;

import static io.avaje.metrics.report.CsvWriteVisitor.*;

/**
 * Writes the metric information as JSON to a buffer for sending.
 */
public class JsonWriter implements MetricStatsVisitor {

  private final int decimalPlaces;

  private final Appendable buffer;

  private boolean includeType;

  private final List<MetricStats> metrics;

  public static void writeTo(Appendable writer, List<MetricStats> metrics) {
    new JsonWriter(writer, metrics).write();
  }

  public JsonWriter(Appendable writer, List<MetricStats> metrics) {
    this(2, writer, metrics);
  }

  public JsonWriter(int decimalPlaces, Appendable writer, List<MetricStats> metrics) {
    this.decimalPlaces = decimalPlaces;
    this.buffer = writer;
    this.metrics = metrics;
  }

  /**
   * Set to true to include the metric type in the JSON output.
   */
  public JsonWriter withType(boolean includeType) {
    this.includeType = includeType;
    return this;
  }

  public void write() {

    try {
      for (int i = 0; i < metrics.size(); i++) {
        if (i == 0) {
          buffer.append("  ");
        } else {
          buffer.append(" ,");
        }
        MetricStats metric = metrics.get(i);
        metric.visit(this);
        buffer.append("\n");
      }
    } catch (IOException e) {
      throw new RuntimeException("Error writing JSON", e);
    }
  }

  private void writeMetricStart(String type, MetricStats metric) throws IOException {
    writeMetricStart(type, metric.name());
  }

  private void writeMetricStart(String type, String name) throws IOException {

    buffer.append("{");
    if (includeType) {
      writeKey("type");
      writeValue(type);
      buffer.append(",");
    }
    writeKey("name");
    writeValue(name);
    buffer.append(",");
  }

  private void writeMetricEnd() {
    try {
      buffer.append("}");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(TimedMetric.Stats metric) {
    try {
      writeMetricStart(TYPE_TIMED_METRIC, metric.nameWithBucket());
      writeSummary(metric);
      writeMetricEnd();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(ValueMetric.Stats metric) {
    try {
      writeMetricStart(TYPE_VALUE_METRIC, metric);
      writeSummary(metric);
      writeMetricEnd();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(CounterMetric.Stats metric) {
    try {
      writeMetricStart(TYPE_COUNTER_METRIC, metric);
      writeKeyNumber("value", metric.count());
      writeMetricEnd();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(GaugeDoubleMetric.Stats metric) {
    try {
      writeMetricStart(TYPE_DOUBLE_METRIC, metric);
      writeKeyNumber("value", format(metric.value()));
      writeMetricEnd();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(GaugeLongMetric.Stats metric) {
    try {
      writeMetricStart(TYPE_LONG_METRIC, metric);
      writeKeyNumber("value", metric.value());
      writeMetricEnd();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void writeSummary(ValueMetric.Stats valueStats) throws IOException {

    // valueStats == null when BucketTimedMetric and the bucket is empty
    long count = (valueStats == null) ? 0 : valueStats.count();

    writeKeyNumber("count", count);
    if (count != 0) {
      buffer.append(",");
      writeKeyNumber("mean", valueStats.mean());
      buffer.append(",");
      writeKeyNumber("max", valueStats.max());
      buffer.append(",");
      writeKeyNumber("total", valueStats.total());
    }
  }

  protected String format(double value) {
    return NumFormat.dp(decimalPlaces, value);
  }

  private void writeKeyNumber(String key, long numberValue) throws IOException {
    writeKeyNumber(key, String.valueOf(numberValue));
  }

  private void writeKeyNumber(String key, String numberValue) throws IOException {
    writeKey(key);
    writeNumberValue(numberValue);
  }

  private void writeKey(String key) throws IOException {
    buffer.append("\"");
    buffer.append(key);
    buffer.append("\":");
  }

  private void writeValue(String val) throws IOException {
    buffer.append("\"");
    buffer.append(val);
    buffer.append("\"");
  }

  private void writeNumberValue(String val) throws IOException {
    buffer.append(val);
  }

}
