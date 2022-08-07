package io.avaje.metrics;

import io.avaje.metrics.spi.SpiMetricProvider;

import java.util.List;
import java.util.ServiceLoader;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;

/**
 * Manages the creation and registration of Metrics.
 * <p>
 * Provides methods to allow agents to go through the registered metrics and gather/report the
 * statistics.
 */
public class MetricManager {

  private static final SpiMetricProvider defaultRegistry = initialiseProvider();

  /**
   * Finds and returns the implementation of PluginMetricManager using the ServiceLoader.
   */
  private static SpiMetricProvider initialiseProvider() {
    return ServiceLoader
      .load(SpiMetricProvider.class)
      .findFirst().orElseThrow(() -> new IllegalStateException("io.avaje.metrics:metrics is not in classpath?"));
  }

  /**
   * Create a new MetricRegistry to attach metrics to.
   */
  public static MetricRegistry createRegistry() {
    return defaultRegistry.createRegistry();
  }

  /**
   * Collect all the metrics from the default registry.
   */
  public static List<MetricStats> collectMetrics() {
    return defaultRegistry.collectMetrics();
  }

  /**
   * Add a metric supplier to the default registry.
   */
  public static void addSupplier(MetricSupplier supplier) {
    defaultRegistry.addSupplier(supplier);
  }

  /**
   * Create a MetricName based on a class and name.
   * <p>
   * Often the name maps to a method name.
   */
  public static String name(Class<?> cls, String name) {
    return cls.getName() + "." +  name;
  }

  /**
   * Return a BucketTimedMetric given the name and bucket ranges using the default registry.
   */
  public static TimedMetric timed(Class<?> cls, String name, int... bucketRanges) {
    return timed(name(cls, name), bucketRanges);
  }

  /**
   * Return a BucketTimedMetric given the name and bucket ranges using the default registry.
   */
  public static TimedMetric timed(String name, int... bucketRanges) {
    return defaultRegistry.timed(name, bucketRanges);
  }

  /**
   * Return a TimedMetric given the name using the default registry.
   */
  public static TimedMetric timed(String name) {
    return defaultRegistry.timed(name);
  }

  /**
   * Return a TimedMetric using the Class, name to derive the MetricName using the default registry.
   */
  public static TimedMetric timed(Class<?> cls, String eventName) {
    return timed(name(cls, eventName));
  }

  /**
   * Return a CounterMetric given the name using the default registry.
   */
  public static CounterMetric counter(String name) {
    return defaultRegistry.counter(name);
  }

  /**
   * Return a CounterMetric using the Class and name to derive the MetricName using the default registry.
   */
  public static CounterMetric counter(Class<?> cls, String eventName) {
    return counter(name(cls, eventName));
  }

  /**
   * Return a ValueMetric given the name using the default registry.
   */
  public static ValueMetric value(String name) {
    return defaultRegistry.value(name);
  }

  /**
   * Return a ValueMetric using the Class and name to derive the MetricName using the default registry.
   */
  public static ValueMetric value(Class<?> cls, String eventName) {
    return value(name(cls, eventName));
  }

  /**
   * Return the TimedMetricGroup with a based metric name using the default registry.
   */
  public static TimedMetricGroup timedGroup(String baseName) {
    return defaultRegistry.timedGroup(baseName);
  }

  /**
   * Return the TimedMetricGroup with a class providing the base metric name using the default registry.
   * <p>
   * The package name is the 'group' and the simple class name the 'type'.
   */
  public static TimedMetricGroup timedGroup(Class<?> cls) {
    return timedGroup(cls.getName());
  }

  /**
   * Create and register a GaugeMetric using the gauge supplied using the default registry.
   */
  public static GaugeDoubleMetric register(String name, DoubleSupplier gauge) {
    return defaultRegistry.register(name, gauge);
  }

  /**
   * Create and register a GaugeCounterMetric using the gauge supplied using the default registry.
   */
  public static GaugeLongMetric register(String name, LongSupplier gauge) {
    return defaultRegistry.register(name, gauge);
  }

  /**
   * Return the built-in JVM metrics support to register collection of all or some
   * of the built-in JVM metrics using the default registry.
   */
  public static JvmMetrics jvmMetrics() {
    return defaultRegistry;
  }

}
