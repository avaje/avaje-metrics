package io.avaje.metrics.report;


import java.io.IOException;
import java.io.Writer;

/**
 * Writes the metric information as JSON to a buffer for sending.
 */
public class JsonWriteVisitor {

  private final Appendable buffer;

  private final JsonWriter jsonWriter;

  private final long collectionTime;

  private final ReportMetrics reportMetrics;

  /**
   * Construct with default formatting of 2 decimal places.
   */
  public JsonWriteVisitor(Writer writer, ReportMetrics reportMetrics) {
    this(2, writer, reportMetrics);
  }

  public JsonWriteVisitor(int decimalPlaces, Appendable writer, ReportMetrics reportMetrics) {
    this.buffer = writer;
    this.jsonWriter = new JsonWriter(decimalPlaces, writer, reportMetrics.getMetrics());
    this.reportMetrics = reportMetrics;
    this.collectionTime = reportMetrics.getCollectionTime();
  }

  public void write() throws IOException {

    buffer.append("{");
    appendHeader();
    writeKey("metrics");
    buffer.append("[\n");
    jsonWriter.write();

    buffer.append("]");
    buffer.append("}");

  }

  private void appendHeader() throws IOException {

    HeaderInfo headerInfo = reportMetrics.getHeaderInfo();
    writeHeader("collected", reportMetrics.getCollectionTime());
    writeHeader("reported", System.currentTimeMillis());
    writeHeader("app", headerInfo.getApp());
    writeHeader("env", headerInfo.getEnv());
    writeHeader("server", headerInfo.getServer());
  }

  private void writeHeader(String key, String value) throws IOException {
    writeKey(key);
    writeValue(value);
    buffer.append(",");
  }

  private  void writeHeader(String key, long value) throws IOException {
    writeKey(key);
    buffer.append(String.valueOf(value));
    buffer.append(",");
  }

  private  void writeKey(String key) throws IOException {
    buffer.append("\"");
    buffer.append(key);
    buffer.append("\":");
  }

  private  void writeValue(String val) throws IOException {
    buffer.append("\"");
    buffer.append(val);
    buffer.append("\"");
  }


}
