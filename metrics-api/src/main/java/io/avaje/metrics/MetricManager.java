package io.avaje.metrics;

import io.avaje.metrics.spi.SpiMetricManager;

import java.util.List;
import java.util.ServiceLoader;

/**
 * Manages the creation and registration of Metrics.
 * <p>
 * Provides methods to allow agents to go through the registered metrics and gather/report the
 * statistics.
 * <p>
 * This uses a service locator to initialise a underlying PluginMetricManager instance. A default
 * implementation of PluginMetricManager is provided by <em>metric</em>.
 */
public class MetricManager {

  /**
   * The implementation that is found via service loader.
   */
  private static final SpiMetricManager mgr = initialiseProvider();

  /**
   * Finds and returns the implementation of PluginMetricManager using the ServiceLoader.
   */
  private static SpiMetricManager initialiseProvider() {
    return ServiceLoader
      .load(SpiMetricManager.class)
      .findFirst().orElseThrow(() -> new IllegalStateException("io.avaje.metrics:metrics is not in classpath?"));
  }

  /**
   * Collect all the metrics.
   */
  public static List<MetricStats> collectMetrics() {
    return mgr.collectMetrics();
  }

  /**
   * When a request completes it is reported to the manager.
   */
  public static void reportTiming(RequestTiming requestTiming) {
    mgr.reportTiming(requestTiming);
  }

  /**
   * Add a metric supplier to the manager. These metrics are then included in the reporting.
   */
  public static void addSupplier(MetricSupplier supplier) {
    mgr.addSupplier(supplier);
  }

  /**
   * Create a MetricName based on a class and name.
   * <p>
   * Often the name maps to a method name.
   */
  public static MetricName name(Class<?> cls, String name) {
    return mgr.name(cls, name);
  }

  /**
   * Create a Metric name by parsing a name that is expected to include periods (dot notation
   * similar to package.Class.method).
   */
  public static MetricName name(String name) {
    return mgr.name(name);
  }

  /**
   * Return a MetricNameCache for the given class.
   * <p>
   * The MetricNameCache can be used to derive MetricName objects dynamically with relatively less
   * overhead.
   * </p>
   */
  public static MetricNameCache nameCache(Class<?> cls) {
    return mgr.nameCache(cls);
  }

  /**
   * Return a MetricNameCache for a given base metric name.
   * <p>
   * The MetricNameCache can be used to derive MetricName objects dynamically with relatively less
   * overhead.
   * </p>
   */
  public static MetricNameCache nameCache(MetricName baseName) {
    return mgr.nameCache(baseName);
  }

  /**
   * Return a BucketTimedMetric given the name and bucket ranges.
   */
  public static TimedMetric timed(MetricName name, int... bucketRanges) {
    return mgr.timed(name, bucketRanges);
  }

  /**
   * Return a BucketTimedMetric given the name and bucket ranges.
   */
  public static TimedMetric timed(Class<?> cls, String name, int... bucketRanges) {
    return timed(name(cls, name), bucketRanges);
  }

  /**
   * Return a BucketTimedMetric given the name and bucket ranges.
   */
  public static TimedMetric timed(String name, int... bucketRanges) {
    return timed(name(name), bucketRanges);
  }

  /**
   * Return a TimedMetric given the name.
   */
  public static TimedMetric timed(MetricName name) {
    return mgr.timed(name);
  }

  /**
   * Return a TimedMetric using the Class, name to derive the MetricName.
   */
  public static TimedMetric timed(Class<?> cls, String eventName) {
    return timed(name(cls, eventName));
  }

  /**
   * Return a TimedMetric given the name.
   */
  public static TimedMetric timed(String name) {
    return timed(name(name));
  }

  /**
   * Return a CounterMetric given the name.
   */
  public static CounterMetric counter(MetricName name) {
    return mgr.counter(name);
  }

  /**
   * Return a CounterMetric given the name.
   */
  public static CounterMetric counter(String name) {
    return counter(name(name));
  }

  /**
   * Return a CounterMetric using the Class and name to derive the MetricName.
   */
  public static CounterMetric counter(Class<?> cls, String eventName) {
    return counter(name(cls, eventName));
  }

  /**
   * Return a ValueMetric given the name.
   */
  public static ValueMetric value(MetricName name) {
    return mgr.value(name);
  }

  /**
   * Return a ValueMetric using the Class and name to derive the MetricName.
   */
  public static ValueMetric value(Class<?> cls, String eventName) {
    return value(name(cls, eventName));
  }

  /**
   * Return a ValueMetric given the name.
   */
  public static ValueMetric value(String name) {
    return value(name(name));
  }

  /**
   * Return the TimedMetricGroup with a based metric name.
   */
  public static TimedMetricGroup timedGroup(MetricName baseName) {
    return mgr.timedGroup(baseName);
  }

  /**
   * Return the TimedMetricGroup with a class providing the base metric name.
   * <p>
   * The package name is the 'group' and the simple class name the 'type'.
   */
  public static TimedMetricGroup timedGroup(Class<?> cls) {
    return timedGroup(name(cls, ""));
  }

  /**
   * Return a TimedMetricGroup with a common group and type name.
   *
   * @param name the metric name
   * @return the TimedMetricGroup used to create TimedMetric's that have a common base name.
   */
  public static TimedMetricGroup timedGroup(String name) {
    return timedGroup(MetricName.of(name));
  }

  /**
   * Create and register a GaugeMetric using the gauge supplied.
   */
  public static GaugeDoubleMetric register(MetricName name, GaugeDouble gauge) {
    return mgr.register(name, gauge);
  }

  /**
   * Create and register a GaugeMetric using the gauge supplied.
   */
  public static GaugeDoubleMetric register(String name, GaugeDouble gauge) {
    return mgr.register(name(name), gauge);
  }

  /**
   * Create and register a GaugeCounterMetric using the gauge supplied.
   */
  public static GaugeLongMetric register(MetricName name, GaugeLong gauge) {
    return mgr.register(name, gauge);
  }

  /**
   * Create and register a GaugeCounterMetric using the gauge supplied.
   */
  public static GaugeLongMetric register(String name, GaugeLong gauge) {
    return mgr.register(name(name), gauge);
  }

//  /**
//   * Return all the non-JVM registered metrics.
//   */
//  public static Collection<Metric> getMetrics() {
//    return mgr.getMetrics();
//  }
//
//  /**
//   * Return the core JVM metrics.
//   */
//  public static Collection<Metric> getJvmMetrics() {
//    return mgr.getJvmMetrics();
//  }

  /**
   * Return the built-in JVM metrics support to register collection of all or some
   * of the built-in JVM metrics.
   */
  public static JvmMetrics jvmMetrics() {
    return mgr;
  }

  /**
   * Return the API for managing request timing.
   */
  public static RequestTimingManager requestTimingManager() {
    return mgr;
  }

}
