package org.avaje.metric.report;

import java.io.IOException;
import java.io.Writer;

/**
 * Writes the metrics in a specific format for a MetricReporter.
 * <p>
 * This can be used to decouple the 'format' from the transport. However this might not be useful in
 * cases where each metric is sent separately via UDP or similar scenarios. This is more useful when
 * all the metrics are sent as a single message.
 */
public interface ReportWriter {

  /**
   * Write the collected metrics in the appropriate format to the writer.
   */
  public void write(Writer writer, ReportMetrics reportMetrics) throws IOException;

}
