package io.avaje.metrics;

import io.avaje.metrics.spi.SpiMetricProvider;

import java.util.List;
import java.util.ServiceLoader;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;

/**
 * Manages the creation and registration of Metrics.
 * <p>
 * Provides access to the global default registry.
 * <p>
 * Provides methods to allow agents to go through the registered metrics and gather/report the
 * statistics.
 */
public class Metrics {

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
   * Create a new registry to create and register metrics to.
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
   * Create a name based on a class and name suffix.
   * <p>
   * Often the name maps to a method name.
   */
  public static String name(Class<?> cls, String name) {
    return cls.getName() + "." +  name;
  }

  /**
   * Return a bucket timer given the name and bucket ranges using the default registry.
   */
  public static Timer timer(Class<?> cls, String name, int... bucketRanges) {
    return timer(name(cls, name), bucketRanges);
  }

  /**
   * Return a bucket timer given the name and bucket ranges using the default registry.
   */
  public static Timer timer(String name, int... bucketRanges) {
    return defaultRegistry.timed(name, bucketRanges);
  }

  /**
   * Return a Timer given the name using the default registry.
   */
  public static Timer timer(String name) {
    return defaultRegistry.timed(name);
  }

  /**
   * Return a Timer using the class, name to derive the MetricName using the default registry.
   */
  public static Timer timer(Class<?> cls, String eventName) {
    return timer(name(cls, eventName));
  }

  /**
   * Return a Counter given the name using the default registry.
   */
  public static Counter counter(String name) {
    return defaultRegistry.counter(name);
  }

  /**
   * Return a Counter using the class and name to derive the name using the default registry.
   */
  public static Counter counter(Class<?> cls, String eventName) {
    return counter(name(cls, eventName));
  }

  /**
   * Return a Meter given the name using the default registry.
   */
  public static Meter meter(String name) {
    return defaultRegistry.value(name);
  }

  /**
   * Return a Meter using the class and name using the default registry.
   */
  public static Meter meter(Class<?> cls, String eventName) {
    return meter(name(cls, eventName));
  }

  /**
   * Return the TimerGroup with a based metric name using the default registry.
   */
  public static TimerGroup timerGroup(String baseName) {
    return defaultRegistry.timedGroup(baseName);
  }

  /**
   * Return the TimerGroup with a class providing the base metric name using the default registry.
   */
  public static TimerGroup timerGroup(Class<?> cls) {
    return timerGroup(cls.getName());
  }

  /**
   * Create and register a gauge with the supplied values using the default registry.
   */
  public static GaugeDouble gauge(String name, DoubleSupplier supplier) {
    return defaultRegistry.gauge(name, supplier);
  }

  /**
   * Create and register a gauge with the supplied long values using the default registry.
   */
  public static GaugeLong gauge(String name, LongSupplier supplier) {
    return defaultRegistry.gauge(name, supplier);
  }

  /**
   * Return the built-in JVM metrics support to register collection of all or some
   * of the built-in JVM metrics using the default registry.
   */
  public static JvmMetrics jvmMetrics() {
    return defaultRegistry;
  }

}
