package io.avaje.metrics.report;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A CSV format report writer.
 * <p>
 * Outputs the collected metrics in CSV format with a variable number of columns depending on the
 * metric type.
 */
public class CsvReportWriter implements ReportWriter {

  protected final SimpleDateFormat nowFormatter;

  protected final int decimalPlaces;

  protected final String delimiter;

  protected final String endOfLine;

  protected final long thresholdMean;

  /**
   * Create as comma delimited with newline character at the end of each line.
   */
  public CsvReportWriter(long thresholdMean) {
    this("HH:mm:ss", 2, ", ", "\n", thresholdMean);
  }

  /**
   * Construct with all the format options.
   *
   * @param timeNowFormat  The date time format for the collection time. Typically this is HH:mm:ss.
   * @param decimalPlaces  The number of decimal places to format double values. This typically defaults to 2.
   * @param delimiter      A string that separates the columns and typically a comma.
   * @param endOfLine      A string added at the end of each metric and typically a newline character.
   * @param thresholdMean  A threshold mean value that can be used to suppress reporting on small metrics.
   */
  public CsvReportWriter(String timeNowFormat, int decimalPlaces, String delimiter, String endOfLine, long thresholdMean) {

    this.nowFormatter = new SimpleDateFormat(timeNowFormat);
    this.decimalPlaces = decimalPlaces;
    this.delimiter = delimiter;
    this.endOfLine = endOfLine;
    this.thresholdMean = thresholdMean;
  }

  /**
   * Write the metrics out in CSV format. Note that some columns come out as name=value pairs in a
   * single column.
   */
  @Override
  public void write(Writer writer, ReportMetrics reportMetrics) throws IOException {

    String timeNowFormatted = nowFormatter.format(new Date());

    CsvWriteVisitor visitor = new CsvWriteVisitor(writer, timeNowFormatted, decimalPlaces, delimiter, endOfLine, thresholdMean);
    visitor.write(reportMetrics);
  }

}