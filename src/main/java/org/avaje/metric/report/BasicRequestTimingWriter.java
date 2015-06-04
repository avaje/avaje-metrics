package org.avaje.metric.report;

import org.avaje.metric.RequestTiming;
import org.avaje.metric.RequestTimingEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Basic writer of the request timings.
 */
public class BasicRequestTimingWriter implements RequestTimingWriter {

  private static Logger logger = LoggerFactory.getLogger(BasicRequestTimingWriter.class);

  SimpleDateFormat nowFormatter = new SimpleDateFormat("HH:mm:ss");

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

  private void writeEntry(Writer writer, RequestTiming requestTiming) throws IOException {

    List<RequestTimingEntry> entries = requestTiming.getEntries();
    int size = entries.size();

    RequestTimingEntry topEntry = entries.get(size - 1);

    long totalExeNanos = topEntry.getExecutionNanos();

    writeHeader(writer, topEntry, requestTiming, totalExeNanos);

    for (int i = size; i > 0; --i) {
      RequestTimingEntry entry = entries.get(i);
      writeDetail(writer, entry, totalExeNanos);
    }

    writeFooter(writer);
  }


  private void writeFooter(Writer writer) throws IOException {
    writer.write("\n");
  }

  private void writeHeader(Writer writer, RequestTimingEntry headerEntry, RequestTiming requestTiming, long totalExeNanos) throws IOException {

    Date reportTime = new Date(requestTiming.getReportTime());

    String eventTime = nowFormatter.format(reportTime);

    writer.write(eventTime);
    writer.write(" ");
    writer.write(String.valueOf(toMillis(totalExeNanos)));
    writer.write(" ");
    writer.write(headerEntry.getMetric().getName().getName());
    writer.write("\n");
  }

  long toMillis(long nanos) {
    return TimeUnit.NANOSECONDS.toMillis(nanos);
  }

  private void writeDetail(Writer writer, RequestTimingEntry entry, long totalExeNanos) throws IOException {

    long executionNanos = entry.getExecutionNanos();

    writer.write("          d:");
    writer.write(entry.getDepth());
    writer.write("   pct:");
    writer.write(String.valueOf(percentage(totalExeNanos, executionNanos)));

    for (int i = 0; i < entry.getDepth(); i++) {
      writer.write("   ");
    }

    writer.write(" exe: ");
    writer.write(String.valueOf(toMillis(executionNanos)));
    writer.write("ms ");
    writer.write(entry.getMetric().getName().getName());
    writer.write("\n");
  }

  private long percentage(long totalExeNanos, long executionNanos) {
    return (100 * executionNanos) / totalExeNanos;
  }

}
