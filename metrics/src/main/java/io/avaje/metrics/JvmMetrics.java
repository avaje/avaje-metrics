package io.avaje.metrics;

/**
 * Standard JVM metrics built in that we often register.
 * <p>
 * Typically we want the standard JVM metrics via {@link #registerJvmMetrics()}.
 * </p>
 * <pre>{@code
 *
 *   Metrics.jvmMetrics()
 *     .withReportAlways()
 *     .registerJvmMetrics();
 *
 * }</pre>
 */
public interface JvmMetrics {

  /**
   * Set to include details for GC and threads.
   */
  JvmMetrics withDetails();

  /**
   * Set to only report when the metrics change.
   */
  JvmMetrics withReportChangesOnly();

  /**
   * Set to report the metrics irrespective of whether the metric has changed.
   * <p>
   * For metrics that generally don't change like max memory or don't change as
   * frequently these metrics will be reported every time.
   * </p>
   */
  JvmMetrics withReportAlways();

  /**
   * Register all the standard JVM metrics - memory, threads, gc, os load and process memory.
   */
  JvmMetrics registerJvmMetrics();

  /**
   * Register a metric for OS load.
   */
  JvmMetrics registerJvmOsLoadMetric();

  /**
   * Register metrics for GC activity.
   */
  JvmMetrics registerJvmGCMetrics();

  /**
   * Register metrics for the total number of threads allocated.
   */
  JvmMetrics registerJvmThreadMetrics();

  /**
   * Register metrics for heap and non-heap memory.
   */
  JvmMetrics registerJvmMemoryMetrics();

  /**
   * Register metrics for VMRSS process memory (if supported on the platform).
   */
  JvmMetrics registerProcessMemoryMetrics();

  /**
   * Register CGroup metrics for CPU usage time, throttle time, requests and limits.
   */
  JvmMetrics registerCGroupMetrics();

}
