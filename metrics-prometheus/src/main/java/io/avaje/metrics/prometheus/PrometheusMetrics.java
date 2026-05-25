package io.avaje.metrics.prometheus;

import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.Metrics;

/**
 * Writes avaje metrics in Prometheus text exposition format.
 *
 * <p>Metrics are collected using cumulative collection so counter, timer count, and timer
 * total values are compatible with Prometheus scrape semantics.
 *
 * <pre>{@code
 * var prometheus = PrometheusMetrics.builder()
 *   .registry(Metrics.registry())
 *   .build();
 *
 * String body = prometheus.scrape();
 * }</pre>
 */
public interface PrometheusMetrics {

  /**
   * Prometheus text exposition content type.
   */
  String CONTENT_TYPE = "text/plain; version=0.0.4; charset=utf-8";

  /**
   * Return a builder for {@link PrometheusMetrics}.
   */
  static Builder builder() {
    return new DPrometheusMetricsBuilder();
  }

  /**
   * Return the metrics scrape body.
   */
  String scrape();

  /**
   * Write the metrics scrape body to the given appendable.
   *
   * @param appendable the appendable to write to
   */
  void write(Appendable appendable);

  /**
   * Builder for {@link PrometheusMetrics}.
   */
  interface Builder {

    /**
     * Specify the {@link MetricRegistry} to collect from.
     *
     * <p>Defaults to the global registry ({@code Metrics.registry()}).
     *
     * @param registry the registry to collect from
     * @return this builder
     */
    Builder registry(MetricRegistry registry);

    /**
     * Set a threshold in microseconds for timer metrics.
     *
     * <p>Timer metrics whose cumulative total duration is less than this threshold
     * will not be reported. Default is 0 (report all timers).
     *
     * <p>This is an advanced option for Prometheus scraping because cumulative
     * collection means the threshold applies to the process-lifetime total.
     *
     * @param threshold the minimum timer total in microseconds to export
     * @return this builder
     */
    Builder timedThresholdMicros(long threshold);

    /**
     * Include windowed {@code max} gauges for timers and meters.
     *
     * <p>Max values reset on each collection, so they are disabled by default. Enable
     * this only when a scrape-window max is useful for the application.
     *
     * @param includeMax whether max gauges should be exported
     * @return this builder
     */
    Builder includeMax(boolean includeMax);

    /**
     * Build and return the {@link PrometheusMetrics}.
     *
     * @return the built Prometheus metrics writer
     */
    PrometheusMetrics build();
  }
}
