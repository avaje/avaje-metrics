package org.avaje.metric.report;


import org.avaje.metric.BucketTimedMetric;
import org.avaje.metric.CounterMetric;
import org.avaje.metric.CounterStatistics;
import org.avaje.metric.GaugeDoubleMetric;
import org.avaje.metric.GaugeLongMetric;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricVisitor;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.ValueMetric;
import org.avaje.metric.ValueStatistics;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Writes the metric information as JSON to a buffer for sending.
 */
public class JsonWriteVisitor implements MetricVisitor {

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
    
    List<Metric> metrics = reportMetrics.getMetrics();
    
    for (int i = 0; i < metrics.size(); i++) {
      if (i == 0) {
        buffer.append("  ");
      } else {
        buffer.append(" ,");
      }
      Metric metric = metrics.get(i);
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

  protected void writeMetricStart(String type, Metric metric) throws IOException {

    buffer.append("{");
    writeKey("type");
    writeValue(type);
    buffer.append(",");
    writeKey("name");
    writeValue(metric.getName().getSimpleName());
    buffer.append(",");
  }

  protected void writeMetricEnd(Metric metric) throws IOException {
    buffer.append("}");
  }

  @Override
  public void visit(TimedMetric metric) throws IOException {

    writeMetricStart("timed", metric);
    if (metric.isBucket()) {
      writeHeader("bucket", metric.getBucketRange());
    }
    writeSummary("norm", metric.getCollectedSuccessStatistics());
    buffer.append(",");
    writeSummary("error", metric.getCollectedErrorStatistics());
    writeMetricEnd(metric);
  }

  @Override
  public void visit(BucketTimedMetric metric) throws IOException {

    TimedMetric[] buckets = metric.getBuckets();
    for (int i = 0; i < buckets.length; i++) {
      if (i > 0) {
        buffer.write(",");
      }
      visit(buckets[i]);
    }
  }

  @Override
  public void visit(ValueMetric metric) throws IOException {

    writeMetricStart("value", metric);
    writeSummary(null, metric.getCollectedStatistics());
    writeMetricEnd(metric);
  }

  @Override
  public void visit(CounterMetric metric) throws IOException {

    writeMetricStart("counter", metric);
    CounterStatistics counterStatistics = metric.getCollectedStatistics();
    writeKeyNumber("count", counterStatistics.getCount());
    buffer.append(",");
    writeKeyNumber("dur", getDuration(counterStatistics.getStartTime()));
    writeMetricEnd(metric);
  }

  @Override
  public void visit(GaugeDoubleMetric metric) throws IOException {

    writeMetricStart("gauge", metric);
    writeKeyNumber("value", format(metric.getValue()));
    writeMetricEnd(metric);
  }

  @Override
  public void visit(GaugeLongMetric metric) throws IOException {

    writeMetricStart("gaugeCounter", metric);
    writeKeyNumber("value", metric.getValue());
    writeMetricEnd(metric);
  }
  
  protected void writeSummary(String prefix, ValueStatistics valueStats) throws IOException {

    // valueStats == null when BucketTimedMetric and the bucket is empty
    long count = (valueStats == null) ? 0 : valueStats.getCount();

    if (prefix != null) {
      writeKey(prefix);
      buffer.append("{");
    }
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
    if (prefix != null) {
      buffer.append("}");
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

  protected void writeKeyString(String key, String value) throws IOException {
    writeKey(key);
    writeValue(value);
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

  /**
   * Return the bucket ranges as a comma delimited list.
   */
  protected String commaDelimited(int[] a) {

    if (a == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < a.length ; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(a[i]);
    }
    return sb.toString();
  }

  protected void writeNumberValue(String val) throws IOException {
    buffer.append(val);
  }

  protected long getDuration(long startTime) {
    return Math.round((System.currentTimeMillis() - startTime) / 1000.0d);
  }

}
