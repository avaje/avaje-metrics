package io.avaje.metrics.report;

import io.avaje.metrics.RequestTiming;
import io.avaje.metrics.RequestTimingEntry;
import io.avaje.metrics.TimedMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Basic writer of the request timings.
 */
public class BasicRequestTimingWriter implements RequestTimingWriter {

  static Logger logger = LoggerFactory.getLogger(BasicRequestTimingWriter.class);

  final SimpleDateFormat nowFormatter = new SimpleDateFormat("HH:mm:ss");

  final int thresholdPercentage;

  public BasicRequestTimingWriter() {
    this(0);
  }

  public BasicRequestTimingWriter(int thresholdPercentage) {
    this.thresholdPercentage = thresholdPercentage;
  }

  /**
   * Write all the request timings to the writer.
   */
  @Override
  public void write(Writer writer, List<RequestTiming> requestTimings) throws IOException {

    try {
      for (RequestTiming requestTiming : requestTimings) {
        writeEntry(writer, requestTiming);
      }

    } catch (IOException e) {
      logger.error("Failed to write request timings", e);
    }
  }

  /**
   * Write the RequestTiming to the writer.
   */
  public void writeEntry(Writer writer, RequestTiming requestTiming) throws IOException {

    // note that the entries are in reverse order
    List<RequestTimingEntry> entries = requestTiming.getEntries();

    Collections.sort(entries);

    RequestTimingEntry topEntry = entries.get(0);
    long totalExeNanos = topEntry.getExecutionNanos();

    writeHeader(writer, topEntry, requestTiming, totalExeNanos);
    // write out reversing the order (to get start based ordering)
    for (int i = 0; i < entries.size(); i++) {
      writeDetail(writer, entries.get(i), totalExeNanos);
    }
    writeFooter(writer);
  }


  /**
   * Write the footer content to the writer.
   */
  protected static void writeFooter(Writer writer) throws IOException {
    writer.write("\n");
  }

  /**
   * Write the header content to the writer.
   */
  protected void writeHeader(Writer writer, RequestTimingEntry headerEntry, RequestTiming requestTiming, long totalExeNanos) throws IOException {

    Date reportTime = new Date(requestTiming.getReportTime());

    String eventTime = nowFormatter.format(reportTime);

    writer.write(eventTime);
    writer.write("  exe:");
    writer.write(String.valueOf(toMillis(totalExeNanos)));
    writer.write("ms  metric:");
    writer.write(headerEntry.getMetric().name().simpleName());

    String requestId = requestTiming.getExternalRequestId();
    if (requestId != null) {
      writer.append("  requestId:").append(requestId);
    }

    writer.write("\n");
  }


  /**
   * Write the detail entry to the writer.
   */
  protected void writeDetail(Writer writer, RequestTimingEntry entry, long totalExeNanos) throws IOException {

    long executionNanos = entry.getExecutionNanos();
    long percentage = percentage(totalExeNanos, executionNanos);

    if (thresholdPercentage > percentage) {
      // skip writing this detail entry as it is deemed not significant enough
      return;
    }

    writer.append("   d:");
    pad(writer, 2, entry.getDepth());
    writer.append("   p:");
    pad(writer, 3, percentage);
    writer.write("  ms:");
    pad(writer, 7, toMillis(executionNanos));
    writer.write("   us:");
    pad(writer, 10, toMicros(executionNanos));
    writer.write("   ");
    for (int i = 0; i < entry.getDepth(); i++) {
      writer.write("   ");
    }

    try {
      writer.append("m:");
      TimedMetric metric = entry.getMetric();
      Map<String, String> attributes = metric.attributes();

      boolean hasAttributes = (attributes != null && !attributes.isEmpty());
      if (!hasAttributes) {
        writer.append(metric.name().simpleName());
      } else {
        int pad = 30 - (entry.getDepth() * 3);
        pad(writer, pad, metric.name().simpleName());
        for (Map.Entry<String, String> attr : attributes.entrySet()) {
          writer.append(" ").append(attr.getKey()).append("[").append(attr.getValue()).append("]");
        }
      }
      writer.write("\n");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Write the value padding with spaces out to padToWidth.
   */
  protected static void pad(Writer writer, int padToWidth, long value) throws IOException {
    pad(writer, padToWidth, String.valueOf(value));
  }

  /**
   * Write the value padding with spaces out to padToWidth.
   */
  protected static void pad(Writer writer, int padToWidth, String s) throws IOException {
    writer.write(s);
    for (int i = 0; i < padToWidth - s.length(); i++) {
      writer.write(" ");
    }
  }

  /**
   * Return the percentage of execution time as a value from 0 to 100.
   */
  protected static long percentage(long totalExeNanos, long executionNanos) {
    return (100 * executionNanos) / totalExeNanos;
  }


  /**
   * Return the nanos as milliseconds.
   */
  protected static long toMillis(long nanos) {
    return TimeUnit.NANOSECONDS.toMillis(nanos);
  }

  /**
   * Return the nanos as microseconds.
   */
  protected static long toMicros(long nanos) {
    return TimeUnit.NANOSECONDS.toMicros(nanos);
  }
}
