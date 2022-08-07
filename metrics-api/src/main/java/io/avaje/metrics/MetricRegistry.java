package io.avaje.metrics;

import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;

/**
 * The SPI for the underlying implementation that is plugged in via service locator.
 */
public interface MetricRegistry extends JvmMetrics {

  /**
   * Create a MetricName based on the class and name.
   * Typically, name is a method name.
   */
  String name(Class<?> cls, String name);

  /**
   * Return the TimedMetric using the metric name.
   */
  Timer timed(String name);

  /**
   * Return the BucketTimedMetric using the given base metric name and bucketRanges.
   *
   * @param name         The metric name
   * @param bucketRanges Time in milliseconds which are used to create buckets.
   */
  Timer timed(String name, int... bucketRanges);

  /**
   * Return the CounterMetric using the metric name.
   */
  Counter counter(String name);

  /**
   * Return the ValueMetric using the metric name.
   */
  Meter value(String name);

  /**
   * Return the TimedMetricGroup using the given base metric name.
   */
  TimerGroup timedGroup(String baseName);



  /**
   * Create and register a GaugeMetric using the gauge supplied (double values).
   */
  GaugeDouble register(String name, DoubleSupplier gauge);

  /**
   * Create and register a GaugeCounterMetric using the gauge supplied (long values).
   */
  GaugeLong register(String name, LongSupplier gauge);

  /**
   * Add a metric supplier.
   */
  void addSupplier(MetricSupplier supplier);

  /**
   * Collect all the metrics.
   */
  List<MetricStats> collectMetrics();

}
