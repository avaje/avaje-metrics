package io.avaje.metrics.graphite;

import io.avaje.metrics.Metric;

import javax.net.SocketFactory;
import java.io.IOException;
import java.util.List;

/**
 * Send metrics to a carbon graphite server.
 */
public interface GraphiteSender {

  /**
   * Return a builder for the GraphiteSender.
   */
  static Builder builder() {
    return new DGraphiteBuilder();
  }

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

  /**
   * Build the GraphiteSender.
   */
  interface Builder {

    /**
     * Use this prefix when sending all metrics (e.g. <code>dev.my-app.</code>).
     * <p>
     * For example, <code>dev.my-app.</code> is a prefix including the environment (dev)
     * and the application name (my-app) along with a trailing period. This prefix will
     * be used when sending all metrics via this sender.
     */
    Builder prefix(String prefix);

    /**
     * The host to send metrics to.
     */
    Builder hostname(String hostname);

    /**
     * The port to send metrics to.
     */
    Builder port(int port);

    /**
     * The SocketFactory to use. Defaults to SSLSocketFactory.getDefault().
     */
    Builder socketFactory(SocketFactory socketFactory);

    /**
     * The batch size to use. Defaults to 100.
     */
    Builder batchSize(int batchSize);

    /**
     * Build and return the GraphiteSender.
     */
    GraphiteSender build();
  }
}
