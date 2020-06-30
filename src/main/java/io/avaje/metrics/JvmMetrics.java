package io.avaje.metrics;

/**
 * Standard JVM metrics built in that we often register.
 * <p>
 * Typically we want the standard JVM metrics and either Logback or Log4J metrics
 * and this provides a relatively easy way to register those.
 * </p>
 * <pre>{@code
 *
 *   MetricManager.jvmMetrics()
 *     .registerStandardJvmMetrics()
 *     .registerLogbackMetrics();
 *
 * }</pre>
 */
public interface JvmMetrics {

  /**
   * Set to include details for GC and threads.
   */
  io.avaje.metrics.JvmMetrics withDetails();

  /**
   * Set to only report when the metrics change. This is the default and means
   * that metrics that don't change are not reported.
   */
  io.avaje.metrics.JvmMetrics withReportChangesOnly();

  /**
   * Set to report the metrics irrespective of whether the metric has changed.
   * <p>
   * For metrics that generally don't change like max memory or don't change as
   * frequently these metrics will be reported every time.
   * </p>
   */
  io.avaje.metrics.JvmMetrics withReportAlways();

  /**
   * Register all the standard JVM metrics - memory, threads, gc, os load and process memory.
   */
  io.avaje.metrics.JvmMetrics registerJvmMetrics();

  /**
   * Register a metric for OS load.
   */
  io.avaje.metrics.JvmMetrics registerJvmOsLoadMetric();

  /**
   * Register metrics for GC activity.
   */
  io.avaje.metrics.JvmMetrics registerJvmGCMetrics();

  /**
   * Register metrics for the total number of threads allocated.
   */
  io.avaje.metrics.JvmMetrics registerJvmThreadMetrics();

  /**
   * Register metrics for heap and non-heap memory.
   */
  io.avaje.metrics.JvmMetrics registerJvmMemoryMetrics();

  /**
   * Register metrics for VMRSS process memory (if supported on the platform).
   */
  io.avaje.metrics.JvmMetrics registerProcessMemoryMetrics();

  /**
   * Register CGroup metrics for CPU usage time, throttle time, requests and limits.
   */
  io.avaje.metrics.JvmMetrics registerCGroupMetrics();

  /**
   * Set the names of the metrics for logging errors and warnings.
   * <p>
   * When not set these default to app.log.error and app.log.warn respectively.
   * </p>
   */
  io.avaje.metrics.JvmMetrics withLogMetricName(String errorMetricName, String warnMetricName);

  /**
   * Register metrics for Logback error and warning message counters.
   */
  io.avaje.metrics.JvmMetrics registerLogbackMetrics();

  /**
   * Register metrics for Log4J error and warning message counters.
   */
  io.avaje.metrics.JvmMetrics registerLog4JMetrics();

}
