package io.avaje.metrics.report;

import io.avaje.metrics.RequestTiming;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Write the collected request timings in the appropriate format to the writer.
 */
public interface RequestTimingWriter {

  /**
   * Write the collected request timings in the appropriate format to the writer.
   */
  void write(Writer writer, List<RequestTiming> requestTimings) throws IOException;

}
