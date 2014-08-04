package org.avaje.metric.report;

import java.io.IOException;
import java.io.Writer;

import org.avaje.metric.BucketTimedMetric;
import org.avaje.metric.CounterMetric;
import org.avaje.metric.CounterStatistics;
import org.avaje.metric.GaugeDoubleGroup;
import org.avaje.metric.GaugeDoubleMetric;
import org.avaje.metric.GaugeLongGroup;
import org.avaje.metric.GaugeLongMetric;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricVisitor;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.ValueMetric;
import org.avaje.metric.ValueStatistics;

/**
 * A visitor that is aimed to write the metrics out in CSV format with different numbers of columns
 * depending on the metric type. Also note that columns contain name=value pairs.
 * <p>
 * This format is aimed at writing to the local file system to provide simple mechanism for
 * reporting of the collected metrics.
 */
public class CsvWriteVisitor implements MetricVisitor {

  /**
   * Code for GaugeLongGroup.
   */
  private static final String TYPE_LONG_GROUP = "lg";

  /**
   * Code for GaugeLongMetric.
   */
  private static final String TYPE_LONG_METRIC = "lm";

  /**
   * Code for GaugeDoubleMetric.
   */
  private static final String TYPE_DOUBLE_METRIC = "dm";

  /**
   * Code for GaugeDoubleGroup.
   */
  private static final String TYPE_DOUBLE_GROUP = "dg";

  /**
   * Code for CounterMetric.
   */
  private static final String TYPE_COUNTER_METRIC = "cm";

  /**
   * Code for ValueMetric.
   */
  private static final String TYPE_VALUE_METRIC = "vm";

  /**
   * The collection time which is typically HH:mm:ss format.
   */
  protected final String collectTimeFormatted;

  /**
   * The time used to calculate metric duration.
   */
  protected final long collectTime;

  /**
   * The number of decimal places to format double values to.
   */
  protected final int decimalPlaces;

  /**
   * Padding defined for the metric names.
   */
  protected final int nameWidth;

  /**
   * Padding defined for the column name=value pairs.
   */
  protected final int columnWidth;

  /**
   * Prefix between delimiters.
   */
  protected final String delimitPrefix;

  /**
   * Suffix between delimiters.
   */
  protected final String delimitSuffix;

  /**
   * The writer that we are writing to.
   */
  protected final Writer writer;

  /**
   * Set to true when reporting error metrics (to get the "err." prefix on the metric name). 
   */
  protected boolean errors;


  /**
   * Construct with some whitespace padding for metric names and columns.
   */
  public CsvWriteVisitor(Writer writer, String timeNowFormat) {
    this(writer, timeNowFormat, 45, 16, 2, "", ", ");
  }

  /**
   * Construct with all the format options.
   */
  public CsvWriteVisitor(Writer writer, String collectTime, int nameWidth, int columnWidth, int decimalPlaces,
      String delimitPrefix, String delimitSuffix) {
    
    this.collectTime = System.currentTimeMillis();
    this.writer = writer;
    this.collectTimeFormatted = collectTime;
    this.decimalPlaces = decimalPlaces;
    this.nameWidth = nameWidth;
    this.columnWidth = columnWidth;
    this.delimitPrefix = "";
    this.delimitSuffix = ", ";
  }

  public void write(ReportMetrics reportMetrics) throws IOException {

    for (Metric metric : reportMetrics.getMetrics()) {
      metric.visit(this);
    }
  }

  protected void writeMetricName(Metric metric, String type) throws IOException {
    writer.write(getNowString());
    writer.write(delimitSuffix);
    writer.write(type);
    writer.write(delimitSuffix);
    writeWithPadding(metric.getName().getSimpleName(), nameWidth);
  }

  protected void writeMetricEnd(Metric metric) throws IOException {
    writer.write("\n");
  }

  @Override
  public void visit(TimedMetric metric) throws IOException {

    writeMetricName(metric, "tm");
    writeSummary("", metric.getCollectedSuccessStatistics());
    writeSummary("err.", metric.getCollectedErrorStatistics());
    writeMetricEnd(metric);
  }

  @Override
  public void visit(BucketTimedMetric metric) throws IOException {

    // Simply visit each of the internal buckets
    TimedMetric[] buckets = metric.getBuckets();
    for (int i = 0; i < buckets.length; i++) {
      visit(buckets[i]);
    }
  }

  @Override
  public void visit(ValueMetric metric) throws IOException {

    writeMetricName(metric, TYPE_VALUE_METRIC);
    writeSummary("", metric.getCollectedStatistics());
    writeMetricEnd(metric);
  }

  @Override
  public void visit(CounterMetric metric) throws IOException {

    writeMetricName(metric, TYPE_COUNTER_METRIC);
    CounterStatistics counterStatistics = metric.getCollectedStatistics();
    write("count", counterStatistics.getCount());
    write("dur", getDuration(counterStatistics.getStartTime()));
    writeMetricEnd(metric);
  }

  @Override
  public void visit(GaugeDoubleGroup gaugeMetricGroup) throws IOException {

    GaugeDoubleMetric[] gaugeMetrics = gaugeMetricGroup.getGaugeMetrics();
    writeMetricName(gaugeMetricGroup, TYPE_DOUBLE_GROUP);
    for (GaugeDoubleMetric m : gaugeMetrics) {
      write(m.getName().getName(), formattedValue(m.getValue()));
    }
    writeMetricEnd(gaugeMetricGroup);
  }

  @Override
  public void visit(GaugeDoubleMetric metric) throws IOException {

    writeMetricName(metric, TYPE_DOUBLE_METRIC);
    write("value", formattedValue(metric.getValue()));
    writeMetricEnd(metric);
  }

  public String formattedValue(double value) {
    return NumFormat.dp(decimalPlaces, value);
  }

  @Override
  public void visit(GaugeLongMetric metric) throws IOException {

    writeMetricName(metric, TYPE_LONG_METRIC);
    write("value", metric.getValue());
    writeMetricEnd(metric);
  }

  @Override
  public void visit(GaugeLongGroup gaugeMetricGroup) throws IOException {

    GaugeLongMetric[] gaugeMetrics = gaugeMetricGroup.getGaugeMetrics();
    writeMetricName(gaugeMetricGroup, TYPE_LONG_GROUP);
    for (GaugeLongMetric m : gaugeMetrics) {
      write(m.getName().getName(), m.getValue());
    }
    writeMetricEnd(gaugeMetricGroup);
  }

  protected void writeSummary(String prefix, ValueStatistics valueStats) throws IOException {

    // valueStats will be null for a BucketTimedMetric with an empty TimedMetric bucket
    long count = (valueStats == null) ? 0 : valueStats.getCount();
    write(prefix, "count", count);
    if (count == 0) {
      return;
    }
    write(prefix, "avg", valueStats.getMean());
    write(prefix, "max", valueStats.getMax());
    write(prefix, "sum", valueStats.getTotal());
    write(prefix, "dur", getDuration(valueStats.getStartTime()));
  }

  protected void write(String prefix, String name, long value) throws IOException {
    writePrefix(prefix);
    write(name, value);
  }

  protected void writePrefix(String prefix) throws IOException {
    if (errors) {
      writer.write("err.");
    }
    if (!prefix.isEmpty()) {
      writer.write(prefix);
    }
  }

  protected void write(String name, long value) throws IOException {
    write(name, String.valueOf(value));
  }

  protected void write(String name, String value) throws IOException {

    writer.write(name);
    writer.write("=");
    writeWithPadding(value, columnWidth - name.length());
  }

  protected void writeWithPadding(String text, int padTo) throws IOException {
    writer.write(text);
    writer.write(delimitSuffix);
    writePadding(text, padTo);
  }

  protected void writePadding(String text, int padTo) throws IOException {
    int extra = padTo - text.length();
    if (extra > 0) {
      for (int i = 0; i < extra; i++) {
        writer.write(" ");
      }
    }
  }

  protected String getNowString() {
    return collectTimeFormatted;
  }

  protected long getDuration(long startTime) {
    return Math.round((collectTime - startTime) / 1000d);
  }

}
