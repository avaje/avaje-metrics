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
   * Prefix between delimiters.
   */
  protected final String delimiter;

  /**
   * Suffix between delimiters.
   */
  protected final String endOfLine;

  /**
   * The writer that we are writing to.
   */
  protected final Writer writer;

  /**
   * A threshold mean value used to suppress reporting of small timed metrics values.
   */
  protected final long thresholdMean;

  /**
   * Set to true when reporting error metrics (to get the "err." prefix on the metric name).
   */
  protected boolean errors;

  /**
   * Construct with a comma delimiter and newline character for the end of each metric.
   *
   * @param writer               The writer where the metrics output is written to.
   * @param collectTimeFormatted The time the metrics were collected. Typically in HH:mm:ss format.
   * @param decimalPlaces        The number of decimal places to format double values. Typically 2.
   */
  public CsvWriteVisitor(Writer writer, String collectTimeFormatted, int decimalPlaces, long thresholdMean) {
    this(writer, collectTimeFormatted, decimalPlaces, ", ", "\n", thresholdMean);
  }

  /**
   * Construct with all the format options.
   *
   * @param writer               The writer where the metrics output is written to.
   * @param collectTimeFormatted The time the metrics were collected. Typically in HH:mm:ss format.
   * @param decimalPlaces        The number of decimal places to format double values. Typically 2.
   * @param delimiter            The delimiter string that prefixes each column.
   * @param endOfLine            The character appended after each metric has been output. Typically the newline character.
   */
  public CsvWriteVisitor(Writer writer, String collectTimeFormatted, int decimalPlaces,
                         String delimiter, String endOfLine, long thresholdMean) {

    this.collectTime = System.currentTimeMillis();
    this.writer = writer;
    this.collectTimeFormatted = collectTimeFormatted;
    this.decimalPlaces = decimalPlaces;
    this.delimiter = delimiter;
    this.endOfLine = endOfLine;
    this.thresholdMean = thresholdMean;
  }

  /**
   * Write the metrics out in CSV format.
   */
  public void write(ReportMetrics reportMetrics) throws IOException {

    for (Metric metric : reportMetrics.getMetrics()) {
      metric.visit(this);
    }
  }

  protected void writeMetricName(Metric metric, String metricTypeCode) throws IOException {

    writer.write(collectTimeFormatted);
    writer.write(delimiter);
    writer.write(metricTypeCode);
    writer.write(delimiter);
    writer.write(metric.getName().getSimpleName());
  }

  protected void writeMetricEnd(Metric metric) throws IOException {
    writer.write(endOfLine);
  }

  @Override
  public void visit(TimedMetric metric) throws IOException {

    ValueStatistics successStats = metric.getCollectedSuccessStatistics();
    ValueStatistics errorStats = metric.getCollectedErrorStatistics();
    if (thresholdMean > 0) {
      if (successStats.getMean() < thresholdMean && errorStats.getMean() < thresholdMean) {
        // suppress reporting based on threshold mean (typically when discovering which
        // metrics we really want to report on etc).
        return;
      }
    }

    writeMetricName(metric, "tm");
    writeSummary("", successStats);
    writeSummary("err.", errorStats);
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
    writePrefix(prefix, "count", count);
    if (count == 0) {
      return;
    }
    writePrefix(prefix, "avg", valueStats.getMean());
    writePrefix(prefix, "max", valueStats.getMax());
    writePrefix(prefix, "sum", valueStats.getTotal());
    writePrefix(prefix, "dur", getDuration(valueStats.getStartTime()));
  }

  protected void writePrefix(String prefix, String name, long value) throws IOException {
    writePrefix(prefix, name, String.valueOf(value));
  }

  protected void writePrefix(String prefix, String name, String value) throws IOException {

    writer.write(delimiter);
    if (errors) {
      writer.write("err.");
    }
    if (!prefix.isEmpty()) {
      writer.write(prefix);
    }
    writer.write(name);
    writer.write("=");
    writer.write(value);
  }

  protected void write(String name, long value) throws IOException {
    write(name, String.valueOf(value));
  }

  protected void write(String name, String value) throws IOException {

    writer.write(delimiter);
    writer.write(name);
    writer.write("=");
    writer.write(value);
  }

  protected long getDuration(long startTime) {
    return Math.round((collectTime - startTime) / 1000d);
  }

  protected String formattedValue(double value) {
    return NumFormat.dp(decimalPlaces, value);
  }
}
