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
   * Set global tags to use with the JVM metrics.
   * <p>
   * By default, this will check the HOSTNAME environment variable and
   * when set, will use this value with a "pod" tag.
   */
  JvmMetrics withGlobalTags(Tags globalTags);

  /**
   * Set to report the metrics irrespective of whether the metric has changed.
   * <p>
   * For metrics that generally don't change like max memory or don't change as
   * frequently, these metrics will be reported every time.
   * </p>
   */
  JvmMetrics withReportAlways();

  /**
   * Register all the standard JVM metrics - memory, threads, gc, os load and process memory.
   */
  JvmMetrics registerJvmMetrics();

  /**
   * Register a metric for OS load.
   *
   * <h4>Base metrics</h4>
   * <ul>
   *   <li>jvm.os.loadAverage</li>
   * </ul>
   */
  JvmMetrics registerJvmOsLoadMetric();

  /**
   * Register metrics for GC activity.
   *
   * <h4>Base metrics</h4>
   * <ul>
   *   <li>jvm.gc.time</li>
   * </ul>
   *
   * <h4>Extra metrics with {@link #withDetails()} enabled</h4>
   * <p>
   *   count and time for each Garbage Collector being used.
   * </p>
   */
  JvmMetrics registerJvmGCMetrics();

  /**
   * Register metrics for the total number of threads allocated.
   *
   * <h4>Base metrics</h4>
   * <ul>
   *   <li>jvm.threads.current</li>
   * </ul>
   *
   * <h4>Extra metrics with {@link #withDetails()} enabled</h4>
   * <ul>
   *   <li>jvm.thread.daemon</li>
   *   <li>jvm.threads.peak</li>
   * </ul>
   */
  JvmMetrics registerJvmThreadMetrics();

  /**
   * Register metrics for heap and non-heap memory.
   *
   * <h4>Base metrics</h4>
   * <ul>
   *   <li>jvm.memory.heap.used</li>
   *   <li>jvm.memory.heap.committed</li>
   *   <li>jvm.memory.heap.max</li>
   *   <li>jvm.memory.nonheap.used</li>
   *   <li>jvm.memory.nonheap.committed</li>
   *   <li>jvm.memory.nonheap.max</li>
   * </ul>
   */
  JvmMetrics registerJvmMemoryMetrics();

  /**
   * Register metrics for VMRSS process memory (if supported on the platform).
   *
   * <h4>Base metrics</h4>
   * <ul>
   *   <li>jvm.memory.process.vmrss</li>
   *   <li>jvm.memory.process.vmhwm</li>
   * </ul>
   */
  JvmMetrics registerProcessMemoryMetrics();

  /**
   * Register CGroup metrics for CPU usage time, throttle time, requests and limits.
   *
   * <h4>Base metrics</h4>
   * <ul>
   *   <li>jvm.cgroup.cpu.requests</li>
   *   <li>jvm.cgroup.cpu.limit (if a limit is applied)</li>
   *   <li>jvm.cgroup.cpu.usageMicros</li>
   *   <li>jvm.cgroup.cpu.throttleMicros</li>
   *   <li>jvm.cgroup.memory.usage</li>
   *   <li>jvm.cgroup.memory.limit/li>
   *   <li>jvm.cgroup.memory.pctUsage/li>
   * </ul>
   *
   * <h4>Extra metrics with {@link #withDetails()} enabled</h4>
   * <ul>
   *   <li>jvm.cgroup.cpu.numPeriod</li>
   *   <li>jvm.cgroup.cpu.numThrottle</li>
   *   <li>jvm.cgroup.cpu.pctThrottle/li>
   * </ul>
   */
  JvmMetrics registerCGroupMetrics();

}
