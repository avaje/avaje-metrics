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
   * Return the Counter using the metric name and tags.
   */
  Counter counter(String name, Tags tags);

  /**
   * Return a builder used to configure and register a counter.
   */
  CounterBuilder counterBuilder(String name);

  /**
   * Return the Meter using the metric name.
   */
  Meter meter(String name);

  /**
   * Return the Meter using the metric name and tags.
   */
  Meter meter(String name, Tags tags);

  /**
   * Return a builder used to configure and register a meter.
   */
  MeterBuilder meterBuilder(String name);

  /**
   * Return a builder used to configure and register a gauge.
   */
  GaugeBuilder gauge(String name);

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
  Timer timer(String name);

  /**
   * Return the timer using the metric name and tags.
   */
  Timer timer(String name, Tags tags);

  /**
   * Return a builder used to configure and register a timer.
   */
  TimerBuilder timerBuilder(String name);

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
   * Collect all the metrics using {@link CollectionMode#DELTA}.
   */
  List<Metric.Statistics> collectMetrics();

  /**
   * Collect all the metrics using the given collection mode.
   */
  List<Metric.Statistics> collectMetrics(CollectionMode mode);

  /**
   * Collect the metrics for writing as JSON using {@link CollectionMode#DELTA}
   * (typically to a supplied Appender).
   */
  JsonMetrics collectAsJson();

  /**
   * Collect the metrics for writing as JSON using the given collection mode
   * (typically to a supplied Appender).
   */
  JsonMetrics collectAsJson(CollectionMode mode);

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
