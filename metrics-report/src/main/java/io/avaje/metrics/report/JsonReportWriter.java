package io.avaje.metrics.report;

import java.io.IOException;
import java.io.Writer;

/**
 * JSON format ReportWriter.
 */
public class JsonReportWriter implements ReportWriter {

  /**
   * The number of decimal places to format double values to.
   */
  protected final int decimalPlaces;

  /**
   * Create using a default of 2 decimal places for formatting double values.
   */
  public JsonReportWriter() {
    this(2);
  }

  /**
   * Create specifying the number of decimal places to format double values.
   */
  public JsonReportWriter(int decimalPlaces) {
    this.decimalPlaces = decimalPlaces;
  }

  /**
   * Write the collected metrics in JSON format to the writer.
   */
  @Override
  public void write(Writer writer, ReportMetrics reportMetrics) throws IOException {

    JsonWriteVisitor visitor = new JsonWriteVisitor(decimalPlaces, writer, reportMetrics);
    visitor.write();

  }

}
