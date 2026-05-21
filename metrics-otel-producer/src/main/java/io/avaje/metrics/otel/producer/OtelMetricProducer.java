package io.avaje.metrics.otel.producer;

import io.avaje.metrics.MetricRegistry;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.export.MetricProducer;

/**
 * Exposes avaje metrics to OpenTelemetry readers using the SDK {@link MetricProducer} bridge.
 *
 * <p>Unlike {@code avaje-metrics-otel}, this bridge aligns collection with the OpenTelemetry SDK
 * reader/exporter interval rather than running on a separate avaje reporting schedule.
 *
 * <p>Register the built producer with {@link SdkMeterProviderBuilder#registerMetricProducer(MetricProducer)}.
 *
 * <p>Because {@link MetricProducer#produce(io.opentelemetry.sdk.resources.Resource)} returns
 * metrics since the previous collection, this bridge is intended for a single reader for a given
 * avaje registry/export path.
 */
public interface OtelMetricProducer extends MetricProducer {

  /**
   * Return a builder for {@link OtelMetricProducer}.
   */
  static Builder builder() {
    return new DOtelMetricProducerBuilder();
  }

  /**
   * Builder for {@link OtelMetricProducer}.
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
     * Set a threshold in microseconds for timed metrics.
     *
     * <p>Timer metrics whose total duration is less than this threshold will not be reported.
     * Default is 0 (report all timers).
     *
     * @param threshold the minimum timer total in microseconds to export
     * @return this builder
     */
    Builder timedThresholdMicros(long threshold);

    /**
     * Set the OpenTelemetry instrumentation scope name.
     *
     * <p>Default is {@code "io.avaje.metrics"}.
     *
     * @param scopeName the instrumentation scope name
     * @return this builder
     */
    Builder scopeName(String scopeName);

    /**
     * Build and return the {@link OtelMetricProducer}.
     *
     * @return the built producer
     */
    OtelMetricProducer build();
  }
}
