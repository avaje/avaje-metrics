package io.avaje.metrics.spi;

import io.avaje.metrics.CounterMetric;
import io.avaje.metrics.GaugeDouble;
import io.avaje.metrics.GaugeDoubleMetric;
import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.GaugeLongMetric;
import io.avaje.metrics.JvmMetrics;
import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricName;
import io.avaje.metrics.MetricNameCache;
import io.avaje.metrics.MetricSupplier;
import io.avaje.metrics.RequestTiming;
import io.avaje.metrics.RequestTimingManager;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.TimedMetricGroup;
import io.avaje.metrics.ValueMetric;
import io.avaje.metrics.statistics.MetricStatistics;
import io.avaje.metrics.statistics.MetricStatisticsAsJson;

import java.util.Collection;
import java.util.List;

/**
 * The SPI for the underlying implementation that is plugged in via service locator.
 */
public interface SpiMetricManager extends JvmMetrics, RequestTimingManager {

  /**
   * Create a MetricName based on the class and name.
   * Typically name is a method name.
   */
  MetricName name(Class<?> cls, String name);

  /**
   * Create a Metric name by parsing a name that is expected to include periods.
   * <p>
   * The name is expected to be in dot notation similar to <code>package.class.method</code>.
   */
  MetricName name(String name);

  /**
   * Return the TimedMetric using the metric name.
   */
  TimedMetric timed(MetricName name);

  /**
   * Return the BucketTimedMetric using the given base metric name and bucketRanges.
   *
   * @param name         The metric name
   * @param bucketRanges Time in milliseconds which are used to create buckets.
   */
  TimedMetric timed(MetricName name, int... bucketRanges);

  /**
   * Return the CounterMetric using the metric name.
   */
  CounterMetric counter(MetricName name);

  /**
   * Return the ValueMetric using the metric name.
   */
  ValueMetric value(MetricName name);

  /**
   * Return the TimedMetricGroup using the given base metric name.
   */
  TimedMetricGroup timedGroup(MetricName baseName);

  /**
   * Return the MetricNameCache using the class as a base name.
   */
  MetricNameCache nameCache(Class<?> cls);

  /**
   * Return the MetricNameCache using a MetricName as a base name.
   */
  MetricNameCache nameCache(MetricName baseName);

  /**
   * Collect all the metrics.
   */
  List<MetricStatistics> collectMetrics();

  /**
   * Return the collection of metrics that are considered non-empty. This means these are metrics
   * that have collected statistics since the last time they were collected.
   * <p>
   * This gets the non emtpy metrics to add themselves to the report list.
   * </p>
   */
  List<MetricStatistics> collectNonEmptyMetrics();

  /**
   * Return the collection of JVM metrics that are non-empty (for reporting).
   */
  List<MetricStatistics> collectNonEmptyJvmMetrics();

  /**
   * Collect the metrics for writing as JSON (typically to a supplied Appender).
   */
  MetricStatisticsAsJson collectAsJson();

  /**
   * Return a collection of all the metrics.
   */
  Collection<Metric> getMetrics();

  /**
   * Return a collection of the JVM metrics.
   */
  Collection<Metric> getJvmMetrics();

  /**
   * Create and register a GaugeMetric using the gauge supplied (double values).
   */
  GaugeDoubleMetric register(MetricName name, GaugeDouble gauge);

  /**
   * Create and register a GaugeCounterMetric using the gauge supplied (long values).
   */
  GaugeLongMetric register(MetricName name, GaugeLong gauge);

  /**
   * When a request completes it is reported to the manager.
   */
  void reportTiming(RequestTiming requestTiming);

  /**
   * Add a metric supplier.
   */
  void addSupplier(MetricSupplier supplier);

}
