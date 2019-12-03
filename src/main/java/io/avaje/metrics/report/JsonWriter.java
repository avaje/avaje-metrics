package io.avaje.metrics.report;


import io.avaje.metrics.statistics.CounterStatistics;
import io.avaje.metrics.statistics.GaugeDoubleStatistics;
import io.avaje.metrics.statistics.GaugeLongStatistics;
import io.avaje.metrics.statistics.MetricStatistics;
import io.avaje.metrics.statistics.MetricStatisticsVisitor;
import io.avaje.metrics.statistics.TimedStatistics;
import io.avaje.metrics.statistics.ValueStatistics;

import java.io.IOException;
import java.util.List;

import static io.avaje.metrics.report.CsvWriteVisitor.TYPE_COUNTER_METRIC;
import static io.avaje.metrics.report.CsvWriteVisitor.TYPE_DOUBLE_METRIC;
import static io.avaje.metrics.report.CsvWriteVisitor.TYPE_LONG_METRIC;
import static io.avaje.metrics.report.CsvWriteVisitor.TYPE_TIMED_METRIC;
import static io.avaje.metrics.report.CsvWriteVisitor.TYPE_VALUE_METRIC;

/**
 * Writes the metric information as JSON to a buffer for sending.
 */
public class JsonWriter implements MetricStatisticsVisitor {

  private final int decimalPlaces;

  private final Appendable buffer;

  private boolean includeType;

  private final List<MetricStatistics> metrics;

  public static void writeTo(Appendable writer, List<MetricStatistics> metrics) {
    new JsonWriter(writer, metrics).write();
  }

  public JsonWriter(Appendable writer, List<MetricStatistics> metrics) {
    this(2, writer, metrics);
  }

  public JsonWriter(int decimalPlaces, Appendable writer, List<MetricStatistics> metrics) {
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
        MetricStatistics metric = metrics.get(i);
        metric.visit(this);
        buffer.append("\n");
      }
    } catch (IOException e) {
      throw new RuntimeException("Error writing JSON", e);
    }
  }

  private void writeMetricStart(String type, MetricStatistics metric) throws IOException {
    writeMetricStart(type, metric.getName());
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
  public void visit(TimedStatistics metric) {
    try {
      writeMetricStart(TYPE_TIMED_METRIC, metric.getNameWithBucket());
      writeSummary(metric);
      writeMetricEnd();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(ValueStatistics metric) {
    try {
      writeMetricStart(TYPE_VALUE_METRIC, metric);
      writeSummary(metric);
      writeMetricEnd();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(CounterStatistics metric) {
    try {
      writeMetricStart(TYPE_COUNTER_METRIC, metric);
      writeKeyNumber("value", metric.getCount());
      writeMetricEnd();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(GaugeDoubleStatistics metric) {
    try {
      writeMetricStart(TYPE_DOUBLE_METRIC, metric);
      writeKeyNumber("value", format(metric.getValue()));
      writeMetricEnd();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(GaugeLongStatistics metric) {
    try {
      writeMetricStart(TYPE_LONG_METRIC, metric);
      writeKeyNumber("value", metric.getValue());
      writeMetricEnd();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void writeSummary(ValueStatistics valueStats) throws IOException {

    // valueStats == null when BucketTimedMetric and the bucket is empty
    long count = (valueStats == null) ? 0 : valueStats.getCount();

    writeKeyNumber("count", count);
    if (count != 0) {
      buffer.append(",");
      writeKeyNumber("mean", valueStats.getMean());
      buffer.append(",");
      writeKeyNumber("max", valueStats.getMax());
      buffer.append(",");
      writeKeyNumber("total", valueStats.getTotal());
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
