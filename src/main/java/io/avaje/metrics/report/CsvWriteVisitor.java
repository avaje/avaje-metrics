package io.avaje.metrics.report;

import io.avaje.metrics.statistics.CounterStatistics;
import io.avaje.metrics.statistics.GaugeDoubleStatistics;
import io.avaje.metrics.statistics.GaugeLongStatistics;
import io.avaje.metrics.statistics.MetricStatistics;
import io.avaje.metrics.statistics.MetricStatisticsVisitor;
import io.avaje.metrics.statistics.TimedStatistics;
import io.avaje.metrics.statistics.ValueStatistics;

import java.io.IOException;
import java.io.Writer;

/**
 * A visitor that is aimed to write the metrics out in CSV format with different numbers of columns
 * depending on the metric type. Also note that columns contain name=value pairs.
 * <p>
 * This format is aimed at writing to the local file system to provide simple mechanism for
 * reporting of the collected metrics.
 */
public class CsvWriteVisitor implements MetricStatisticsVisitor {

  /**
   * Code for GaugeLongMetric.
   */
  private static final String TYPE_LONG_METRIC = "lm";

  /**
   * Code for GaugeDoubleMetric.
   */
  private static final String TYPE_DOUBLE_METRIC = "dm";

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
  public void write(ReportMetrics reportMetrics) {

    for (MetricStatistics metric : reportMetrics.getMetrics()) {
      metric.visit(this);
    }
  }

  protected void writeMetricName(MetricStatistics metric, String metricTypeCode) throws IOException {

    writer.write(collectTimeFormatted);
    writer.write(delimiter);
    writer.write(metricTypeCode);
    writer.write(delimiter);
    writer.write(metric.getName());
  }

  protected void writeMetricEnd(MetricStatistics metric) throws IOException {
    writer.write(endOfLine);
  }

  @Override
  public void visit(TimedStatistics metric) {

    try {
      if (thresholdMean > 0) {
        if (metric.getMean() < thresholdMean) {
          // suppress reporting based on threshold mean (typically when discovering which
          // metrics we really want to report on etc).
          return;
        }
      }

      writeMetricName(metric, "tm");
      if (metric.isBucket()) {
        writer.write("[");
        writer.write(metric.getBucketRange());
        writer.write("]");
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
      writeMetricName(metric, TYPE_VALUE_METRIC);
      writeSummary(metric);
      writeMetricEnd(metric);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(CounterStatistics metric) {

    try {
      writeMetricName(metric, TYPE_COUNTER_METRIC);
      write("count", metric.getCount());
      write("dur", getDuration(metric.getStartTime()));
      writeMetricEnd(metric);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(GaugeDoubleStatistics metric) {
    try {
      writeMetricName(metric, TYPE_DOUBLE_METRIC);
      writeValue(formattedValue(metric.getValue()));
      writeMetricEnd(metric);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(GaugeLongStatistics metric) {
    try {
      writeMetricName(metric, TYPE_LONG_METRIC);
      writeValue(String.valueOf(metric.getValue()));
      writeMetricEnd(metric);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void writeSummary(ValueStatistics valueStats) throws IOException {

    long count = valueStats.getCount();
    write("count", count);
    if (count == 0) {
      return;
    }
    write("avg", valueStats.getMean());
    write("max", valueStats.getMax());
    write("sum", valueStats.getTotal());
    write("dur", getDuration(valueStats.getStartTime()));
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

  protected void writeValue(String value) throws IOException {
    writer.write(delimiter);
    writer.write(value);
  }

  protected long getDuration(long startTime) {
    return Math.round((collectTime - startTime) / 1000.0d);
  }

  protected String formattedValue(double value) {
    return NumFormat.dp(decimalPlaces, value);
  }
}
