package io.avaje.metrics;

import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.LongSupplier;

/**
 * The SPI for the underlying implementation that is plugged in via service locator.
 */
public interface MetricRegistry extends JvmMetrics {

  /**
   * Return the Counter using the metric name.
   */
  Counter counter(String name);

  /**
   * Return the Counter with the given name and tags.
   */
  Counter counter(String name, Tags tags);

  /**
   * Return the Meter using the metric name.
   */
  Meter meter(String name);

  /**
   * Return the Meter using the metric name and tags.
   */
  Meter meter(String name, Tags tags);

  /**
   * Create and register a gauge using the supplied double values.
   */
  GaugeDouble gauge(String name, DoubleSupplier supplier);

  /**
   * Create and register a gauge using the supplied double values.
   */
  GaugeDouble gauge(String name, Tags tags, DoubleSupplier supplier);

  /**
   * Create and register a gauge using the supplied long values.
   */
  GaugeLong gauge(String name, LongSupplier supplier);

  /**
   * Create and register a gauge using the supplied long values.
   */
  GaugeLong gauge(String name, Tags tags, LongSupplier supplier);

  /**
   * Return the timer using the metric name.
   */
  Timer timer(String name);

  /**
   * Return the timer using the metric name and tags.
   */
  Timer timer(String name, Tags tags);

  /**
   * Return the bucket timer using the given base metric name and bucketRanges.
   *
   * @param name         The metric name
   * @param bucketRanges Time in milliseconds which are used to create buckets.
   */
  Timer timer(String name, int... bucketRanges);

  /**
   * Return the bucket timer using the given base metric name, tags and bucketRanges.
   *
   * @param name         The metric name
   * @param tags         The metric tags
   * @param bucketRanges Time in milliseconds which are used to create buckets.
   */
  Timer timer(String name, Tags tags, int... bucketRanges);

  /**
   * Return the TimerGroup using the given base metric name.
   */
  TimerGroup timerGroup(String baseName);

  /**
   * Register a metric that was created externally with the registry.
   */
  void register(Metric metric);

  /**
   * Add an external metric supplier.
   */
  void addSupplier(MetricSupplier supplier);

  /**
   * Set the naming convention to use when reporting metrics.
   */
  MetricRegistry naming(Function<String, String> namingConvention);

  /**
   * Use the underscore naming convention for reporting metrics.
   * <p>
   * This converts period to underscore.
   */
  MetricRegistry namingUnderscore();

  /**
   * Collect all the metrics.
   */
  List<Metric.Statistics> collectMetrics();

  /**
   * Collect the metrics for writing as JSON (typically to a supplied Appender).
   */
  JsonMetrics collectAsJson();

  /**
   * Write the collected metrics in JSON form.
   */
  interface JsonMetrics {

    /**
     * Write the metrics in JSON form to the given appendable.
     * <p>
     * Note that this doesn't add JSON array start and end so
     * those need to be added as needed.
     * </p>
     *
     * @param appendable The buffer to write the metrics to
     */
    void write(Appendable appendable);

    /**
     * Collect and return the metrics as JSON array content.
     */
    String asJson();
  }

}
