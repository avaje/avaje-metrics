package io.avaje.metrics.otel;

import io.avaje.metrics.MetricRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;

import java.util.concurrent.TimeUnit;

/**
 * Reports avaje metrics to OpenTelemetry using the OTEL SDK Metrics API.
 *
 * <p>The reporter collects avaje metrics periodically and pushes them to the provided
 * OpenTelemetry {@link MeterProvider}. The OTEL SDK and its configured exporters
 * (OTLP, Prometheus, etc.) are responsible for shipping the data.
 *
 * <pre>{@code
 *
 *   OtelReporter reporter = OtelReporter.builder()
 *       .openTelemetry(openTelemetry)
 *       .schedule(60, TimeUnit.SECONDS)
 *       .timedThresholdMicros(1_000)
 *       .build();
 *
 *   reporter.start();
 *
 *   // On shutdown:
 *   reporter.close();
 *
 * }</pre>
 */
public interface OtelReporter extends AutoCloseable {

  /**
   * Return a builder for the OtelReporter.
   */
  static Builder builder() {
    return new DOtelBuilder();
  }

  /**
   * Collect all metrics and push them to OpenTelemetry once.
   * <p>
   * This can be called directly if you prefer to manage the reporting schedule yourself.
   */
  void report();

  /**
   * Start periodic reporting on the configured schedule.
   */
  void start();

  /**
   * Stop periodic reporting.
   */
  void close();

  /**
   * Builder for {@link OtelReporter}.
   */
  interface Builder {

    /**
     * Specify the {@link OpenTelemetry} instance to use.
     * <p>
     * This is the preferred way to configure the reporter. If not set, defaults to
     * {@code GlobalOpenTelemetry.getOrNoop()}.
     */
    Builder openTelemetry(OpenTelemetry openTelemetry);

    /**
     * Specify the {@link MeterProvider} to use directly.
     * <p>
     * Use this as an alternative to {@link #openTelemetry(OpenTelemetry)} when you only
     * have access to the MeterProvider.
     */
    Builder meterProvider(MeterProvider meterProvider);

    /**
     * Specify the {@link MetricRegistry} to report from.
     * <p>
     * Defaults to the global registry ({@code Metrics.registry()}).
     */
    Builder registry(MetricRegistry registry);

    /**
     * Specify the reporting schedule. Default is 60 seconds.
     */
    Builder schedule(int schedule, TimeUnit timeUnit);

    /**
     * Set a threshold in microseconds for timed metrics.
     * <p>
     * Timer metrics whose total duration is less than this threshold will not be reported.
     * Default is 0 (report all timers).
     * <p>
     * For example, setting to {@code 1_000} (1 millisecond) when reporting every 60 seconds
     * will suppress methods with less than 1ms total execution time per reporting period.
     */
    Builder timedThresholdMicros(long threshold);

    /**
     * Set the OpenTelemetry instrumentation scope name. Default is {@code "io.avaje.metrics"}.
     */
    Builder scopeName(String scopeName);

    /**
     * Build and return the {@link OtelReporter}.
     */
    OtelReporter build();
  }
}
