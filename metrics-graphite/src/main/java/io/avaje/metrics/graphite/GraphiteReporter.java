package io.avaje.metrics.graphite;

import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.MetricSupplier;
import io.ebean.Database;

import javax.net.SocketFactory;

/**
 * Report metrics to a carbon graphite server.
 *
 * <pre>{@code
 *
 *   GraphiteReporter reporter = GraphiteReporter.builder()
 *       .prefix("dev.myApp.")
 *       .hostname(grafanaHost)
 *       .port(grafanaPort)
 *       .build();
 *
 *
 *   // periodically report the metrics
 *   reporter.report();
 *
 * }</pre>
 */
public interface GraphiteReporter {

  /**
   * Return the builder to create a GraphiteReporter.
   *
   * <pre>{@code
   *
   *   GraphiteReporter reporter = GraphiteReporter.builder()
   *       .prefix("dev.myApp.")
   *       .hostname(grafanaHost)
   *       .port(grafanaPort)
   *       .build();
   *
   *
   *   // periodically report the metrics
   *   reporter.report();
   *
   * }</pre>
   */
  static GraphiteReporter.Builder builder() {
    return new DGraphiteBuilder();
  }

  /**
   * Report the metrics.
   * <p>
   * An error reporting the metrics is captured and logged. This method
   * does not throw an exception.
   */
  void report();

  /**
   * Build the GraphiteReporter.
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
     * The batch size to use. Defaults to 500.
     */
    Builder batchSize(int batchSize);

    /**
     * Use this to stop the default metrics registry from being included
     * in metrics reporting.
     */
    Builder excludeDefaultRegistry();

    /**
     * Include Ebean Database metrics in the reporting.
     * <p>
     * This can be used multiple times for each database that we want to
     * report metrics from.
     */
    Builder database(Database database);

    /**
     * Include the metric registry.
     * <p>
     * Typically, this is only used for the non-default metrics registry.
     *
     * @see #excludeDefaultRegistry()
     */
    Builder registry(MetricRegistry registry);

    /**
     * Include metrics from the MetricSupplier
     */
    Builder registry(MetricSupplier supplier);

    /**
     * Build and return the Reporter.
     */
    GraphiteReporter build();
  }
}
