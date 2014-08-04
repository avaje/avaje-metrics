package org.avaje.metric.report;

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

  protected final int nameWidth;

  protected final int columnWidth;

  protected final String delimitPrefix;

  protected final String delimitSuffix;

  /**
   * Create with some padding for the metrics names and columns (somewhat pretty format).
   */
  public CsvReportWriter() {
    this("HH:mm:ss", 45, 16, 2, "", ", ");
  }

  /**
   * Create with the option of compact form or with some whitespace padding.
   */
  public CsvReportWriter(boolean compact) {
    this("HH:mm:ss", compact ? 1 : 45, compact ? 1 : 16, 2, "", ",");
  }

  /**
   * Construct with all the format options.
   * 
   * @param timeNowFormat
   *          The date time format for the collection time. Typically this is HH:mm:ss.
   * @param nameWidth
   *          Controls whitespace padding on the metric name.
   * @param columnWidth
   *          Controls the whitespace passing on the metric name=value columns.
   * @param decimalPlaces
   *          The number of decimal places to format double values. This typically defaults to 2.
   * @param delimitPrefix
   *          A string prefix added between delimiters.
   * @param delimitSuffix
   *          A string suffix added between delimiters.
   */
  public CsvReportWriter(String timeNowFormat, int nameWidth, int columnWidth, int decimalPlaces, String delimitPrefix, String delimitSuffix) {

    this.nowFormatter = new SimpleDateFormat(timeNowFormat);
    this.nameWidth = nameWidth;
    this.columnWidth = columnWidth;
    this.decimalPlaces = decimalPlaces;
    this.delimitPrefix = delimitPrefix;
    this.delimitSuffix = delimitSuffix;
  }

  /**
   * Write the metrics out in CSV format. Note that some columns come out as name=value pairs in a
   * single column.
   */
  @Override
  public void write(Writer writer, ReportMetrics reportMetrics) throws IOException {

    String timeNowFormatted = nowFormatter.format(new Date());

    CsvWriteVisitor visitor = new CsvWriteVisitor(writer, timeNowFormatted, nameWidth, columnWidth, decimalPlaces, delimitPrefix, delimitSuffix);
    visitor.write(reportMetrics);
  }

}
