package io.avaje.metrics.report;

import io.avaje.metrics.*;

import java.io.IOException;
import java.io.Writer;

/**
 * A visitor that is aimed to write the metrics out in CSV format with different numbers of columns
 * depending on the metric type. Also note that columns contain name=value pairs.
 * <p>
 * This format is aimed at writing to the local file system to provide simple mechanism for
 * reporting of the collected metrics.
 */
public class CsvWriteVisitor implements MetricStatsVisitor {

  /**
   * Code for GaugeLongMetric.
   */
  static final String TYPE_LONG_METRIC = "lm";

  /**
   * Code for GaugeDoubleMetric.
   */
  static final String TYPE_DOUBLE_METRIC = "dm";

  /**
   * Code for CounterMetric.
   */
  static final String TYPE_COUNTER_METRIC = "cm";

  /**
   * Code for ValueMetric.
   */
  static final String TYPE_VALUE_METRIC = "vm";

  static final String TYPE_TIMED_METRIC = "tm";

  /**
   * The collection time which is typically HH:mm:ss format.
   */
  private final String collectTimeFormatted;

  /**
   * The number of decimal places to format double values to.
   */
  private final int decimalPlaces;

  /**
   * Prefix between delimiters.
   */
  private final String delimiter;

  /**
   * Suffix between delimiters.
   */
  private final String endOfLine;

  /**
   * The writer that we are writing to.
   */
  private final Writer writer;

  /**
   * A threshold mean value used to suppress reporting of small timed metrics values.
   */
  private final long thresholdMean;

  private boolean includeType;

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

    this.writer = writer;
    this.collectTimeFormatted = collectTimeFormatted;
    this.decimalPlaces = decimalPlaces;
    this.delimiter = delimiter;
    this.endOfLine = endOfLine;
    this.thresholdMean = thresholdMean;
  }

  /**
   * Set to true to include the metric type in the JSON output.
   */
  public CsvWriteVisitor withType(boolean includeType) {
    this.includeType = includeType;
    return this;
  }

  /**
   * Write the metrics out in CSV format.
   */
  public void write(ReportMetrics reportMetrics) {

    for (MetricStats metric : reportMetrics.getMetrics()) {
      metric.visit(this);
    }
  }

  private void writeMetricName(MetricStats metric, String metricTypeCode) throws IOException {
    writeMetricName(metric.name(), null, metricTypeCode);
  }

  private void writeMetricName(String metricName, String bucket, String metricTypeCode) throws IOException {
    writer.write(collectTimeFormatted);
    writer.write(delimiter);
    if (includeType) {
      writer.write(metricTypeCode);
      writer.write(delimiter);
    }
    writer.write(metricName);
    if (bucket != null) {
      writer.write(";bucket=");
      writer.write(bucket);
    }
  }

  private void writeMetricEnd() throws IOException {
    writer.write(endOfLine);
  }

  @Override
  public void visit(Timer.Stats metric) {
    try {
      if (thresholdMean > 0) {
        if (metric.mean() < thresholdMean) {
          // suppress reporting based on threshold mean (typically when discovering which
          // metrics we really want to report on etc).
          return;
        }
      }
      writeMetricName(metric.name(), metric.bucketRange(), TYPE_TIMED_METRIC);
      writeSummary(metric);
      writeMetricEnd();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(Meter.Stats metric) {

    try {
      writeMetricName(metric, TYPE_VALUE_METRIC);
      writeSummary(metric);
      writeMetricEnd();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(Counter.Stats metric) {

    try {
      writeMetricName(metric, TYPE_COUNTER_METRIC);
      writeValue(String.valueOf(metric.count()));
      writeMetricEnd();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(GaugeDouble.Stats metric) {
    try {
      writeMetricName(metric, TYPE_DOUBLE_METRIC);
      writeValue(formattedValue(metric.value()));
      writeMetricEnd();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(GaugeLong.Stats metric) {
    try {
      writeMetricName(metric, TYPE_LONG_METRIC);
      writeValue(String.valueOf(metric.value()));
      writeMetricEnd();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void writeSummary(Meter.Stats valueStats) throws IOException {

    long count = valueStats.count();
    write("count", count);
    if (count == 0) {
      return;
    }
    write("mean", valueStats.mean());
    write("max", valueStats.max());
    write("total", valueStats.total());
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

  private void writeValue(String value) throws IOException {
    writer.write(delimiter);
    writer.write(value);
  }

  private String formattedValue(double value) {
    return NumFormat.dp(decimalPlaces, value);
  }
}
