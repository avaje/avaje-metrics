package io.avaje.metrics.graphite;

import io.avaje.metrics.Metric;

import java.io.IOException;
import java.util.List;

/**
 * Send metrics to a carbon graphite server.
 */
interface GraphiteSender {

  /**
   * Return true if the sender is connected.
   */
  boolean isConnected();

  /**
   * Connect to the server.
   */
  void connect() throws IllegalStateException, IOException;

  /**
   * Sanitize a name and return it.
   */
  String sanitize(String string);

  /**
   * Send the metric. Note that the names of the metric are concatenated together at send time along with the
   * global prefix and typically the names include a period to separate them - e.g. {@code dev.my-app.some-metric.total }.
   *
   * @param value     The value of the metric
   * @param epochSecs The timestamp in epoch seconds
   * @param names     The names of the metric are concatenated together at send time along with the global prefix.
   */
  void send(String value, long epochSecs, String... names) throws IOException;

  /**
   * Send all the metrics.
   */
  void send(List<Metric.Statistics> metrics);

  /**
   * Flush and send all the metrics.
   */
  void flush() throws IOException;

  /**
   * Close the connection to the server.
   */
  void close() throws IOException;

  interface Reporter {

    void report(GraphiteSender sender) throws IOException;
  }
}
