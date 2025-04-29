package io.avaje.metrics.statsd;

import com.timgroup.statsd.StatsDClient;
import io.avaje.metrics.MetricRegistry;
import io.ebean.Database;

import java.util.concurrent.TimeUnit;

/**
 * Interface for a StatsD reporter that can be used to report metrics.
 * <p>
 * This interface allows for the creation of a StatsD reporter with various configurations such as hostname,
 * port, client, tags, and reporting schedule.
 * <p>
 * The reporter can be started and stopped, and it supports custom reporters that can be included in the
 * reporting process.
 */
public interface StatsdReporter extends AutoCloseable {

  /**
   * Create a builder for the reporter.
   */
  static Builder builder() {
    return new StatsdBuilder();
  }

  /**
   * Start reporting.
   */
  void start();

  /**
   * Shutdown and stop reporting.
   */
  void close();

  /**
   * Custom reporters that can be included.
   */
  interface Reporter {

    /**
     * Report the metrics to the client.
     */
    void report(StatsDClient statsdClient);
  }

  /**
   * Builder for the StatsdReporter.
   */
  interface Builder {

    /**
     * Specify the hostname to use. Default is localhost.
     */
    Builder hostname(String hostname);

    /**
     * Specify the port to use. Default is 8125.
     */
    Builder port(int port);

    /**
     * Specify the common tags to be used on all the reported metrics. Default is no tags.
     */
    Builder tags(String[] tags);

    /**
     * Specify the StatsD client to use. Default is a NonBlockingStatsDClient.
     * <p>
     * When using a custom client, the hostname, port and tags are not used.
     */
    Builder client(StatsDClient client);

    /**
     * Specify the threshold in microseconds for timed metrics to be reported. Default is 0.
     * <p>
     * Set this to reduce the number of metrics reported for timed metrics.
     * <p>
     * For example, if set to 10_000, metrics with a duration less than 10 milliseconds will not be reported.
     */
    Builder timedThresholdMicros(long timedThreshold);

    /**
     * Specify the schedule in seconds. Default is 60 seconds.
     */
    Builder schedule(int schedule, TimeUnit timeUnit);

    /**
     * Specify the Metrics registry to use. If not specified the global registry is used.
     */
    Builder registry(MetricRegistry registry);

    /**
     * Add a database to report on.
     */
    Builder database(Database database);

    /**
     * Add a database to report on with verbose metrics.
     */
    Builder databaseVerbose(Database database);

    /**
     * Add an additional custom reporter.
     */
    Builder reporter(Reporter reporter);

    /**
     * Build the reporter.
     */
    StatsdReporter build();

  }
}
