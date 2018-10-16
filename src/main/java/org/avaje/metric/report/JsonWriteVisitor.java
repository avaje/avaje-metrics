package org.avaje.metric.report;


import org.avaje.metric.statistics.CounterStatistics;
import org.avaje.metric.statistics.GaugeDoubleStatistics;
import org.avaje.metric.statistics.GaugeLongStatistics;
import org.avaje.metric.statistics.MetricStatistics;
import org.avaje.metric.statistics.MetricStatisticsVisitor;
import org.avaje.metric.statistics.TimedStatistics;
import org.avaje.metric.statistics.ValueStatistics;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Writes the metric information as JSON to a buffer for sending.
 */
public class JsonWriteVisitor implements MetricStatisticsVisitor {

  protected final int decimalPlaces;

  protected final Writer buffer;

  protected final long collectionTime;

  protected final ReportMetrics reportMetrics;

  /**
   * Construct with default formatting of 2 decimal places.
   */
  public JsonWriteVisitor(Writer writer, ReportMetrics reportMetrics) {
    this(2, writer, reportMetrics);
  }

  public JsonWriteVisitor(int decimalPlaces, Writer writer, ReportMetrics reportMetrics) {
    this.decimalPlaces = decimalPlaces;
    this.buffer = writer;
    this.reportMetrics = reportMetrics;
    this.collectionTime = reportMetrics.getCollectionTime();
  }

  public void write() throws IOException {

    buffer.append("{");
    appendHeader();
    writeKey("metrics");
    buffer.append("[\n");
    appendMetricsJson();
    buffer.append("]");
    buffer.append("}");

  }

  protected void appendMetricsJson() throws IOException {

    List<MetricStatistics> metrics = reportMetrics.getMetrics();

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
  }

  protected void appendHeader() throws IOException {

    HeaderInfo headerInfo = reportMetrics.getHeaderInfo();
    writeHeader("collected", reportMetrics.getCollectionTime());
    writeHeader("reported", System.currentTimeMillis());
    writeHeader("app", headerInfo.getApp());
    writeHeader("env", headerInfo.getEnv());
    writeHeader("server", headerInfo.getServer());
  }

  protected void writeMetricStart(String type, MetricStatistics metric) throws IOException {

    buffer.append("{");
    writeKey("type");
    writeValue(type);
    buffer.append(",");
    writeKey("name");
    writeValue(metric.getName());
    buffer.append(",");
  }

  protected void writeMetricEnd(MetricStatistics metric) {
    try {
      buffer.append("}");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(TimedStatistics metric) {
    try {
      writeMetricStart("timed", metric);
      if (metric.isBucket()) {
        writeHeader("bucket", metric.getBucketRange());
      }
      writeSummary(metric);
      writeMetricEnd(metric);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(ValueStatistics metric) {
    try {
      writeMetricStart("value", metric);
      writeSummary(metric);
      writeMetricEnd(metric);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(CounterStatistics metric) {
    try {
      writeMetricStart("counter", metric);
      writeKeyNumber("count", metric.getCount());
      buffer.append(",");
      writeKeyNumber("dur", getDuration(metric.getStartTime()));
      writeMetricEnd(metric);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(GaugeDoubleStatistics metric) {
    try {
      writeMetricStart("gauge", metric);
      writeKeyNumber("value", format(metric.getValue()));
      writeMetricEnd(metric);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(GaugeLongStatistics metric) {
    try {
      writeMetricStart("gaugeCounter", metric);
      writeKeyNumber("value", metric.getValue());
      writeMetricEnd(metric);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void writeSummary(ValueStatistics valueStats) throws IOException {

    // valueStats == null when BucketTimedMetric and the bucket is empty
    long count = (valueStats == null) ? 0 : valueStats.getCount();

    writeKeyNumber("count", count);
    if (count != 0) {
      buffer.append(",");
      writeKeyNumber("avg", valueStats.getMean());
      buffer.append(",");
      writeKeyNumber("max", valueStats.getMax());
      buffer.append(",");
      writeKeyNumber("sum", valueStats.getTotal());
      buffer.append(",");
      writeKeyNumber("dur", getDuration(valueStats.getStartTime()));
    }
  }

  protected String format(double value) {
    return NumFormat.dp(decimalPlaces, value);
  }

  protected void writeKeyNumber(String key, long numberValue) throws IOException {
    writeKeyNumber(key, String.valueOf(numberValue));
  }

  protected void writeKeyNumber(String key, String numberValue) throws IOException {
    writeKey(key);
    writeNumberValue(numberValue);
  }

  public void writeHeader(String key, String value) throws IOException {
    writeKey(key);
    writeValue(value);
    buffer.append(",");
  }

  public void writeHeader(String key, long value) throws IOException {
    writeKey(key);
    buffer.append(String.valueOf(value));
    buffer.append(",");
  }

  protected void writeKey(String key) throws IOException {
    buffer.append("\"");
    buffer.append(key);
    buffer.append("\":");
  }

  protected void writeValue(String val) throws IOException {
    buffer.append("\"");
    buffer.append(val);
    buffer.append("\"");
  }

  protected void writeNumberValue(String val) throws IOException {
    buffer.append(val);
  }

  protected long getDuration(long startTime) {
    return Math.round((System.currentTimeMillis() - startTime) / 1000.0d);
  }

}
