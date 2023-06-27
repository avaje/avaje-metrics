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
   * Return the Meter using the metric name.
   */
  Meter meter(String name);

  /**
   * Create and register a gauge using the supplied double values.
   */
  GaugeDouble gauge(String name, DoubleSupplier supplier);

  /**
   * Create and register a gauge using the supplied long values.
   */
  GaugeLong gauge(String name, LongSupplier supplier);

  /**
   * Return the timer using the metric name.
   */
  Timer timed(String name);

  /**
   * Return the bucket timer using the given base metric name and bucketRanges.
   *
   * @param name         The metric name
   * @param bucketRanges Time in milliseconds which are used to create buckets.
   */
  Timer timed(String name, int... bucketRanges);

  /**
   * Return the TimerGroup using the given base metric name.
   */
  TimerGroup timedGroup(String baseName);

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
