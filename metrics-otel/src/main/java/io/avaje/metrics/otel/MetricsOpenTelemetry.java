package io.avaje.metrics.otel;

import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.Metrics;
import io.avaje.metrics.otel.producer.OtelMetricProducer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.OpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Convenience builder for creating an {@link OpenTelemetrySdk} configured with OTLP trace export
 * and {@link OtelMetricProducer} metrics export.
 *
 * <p>This helper is intentionally focused on the common OTLP case:
 *
 * <ul>
 *   <li>one OTLP endpoint for traces and metrics</li>
 *   <li>one {@link Resource} carrying {@code service.name} applied to both tracer and meter providers</li>
 *   <li>{@link OtelMetricProducer} registered on the {@link SdkMeterProvider}</li>
 * </ul>
 *
 * <p>If you need more control, you can wire the OpenTelemetry SDK manually and use
 * {@link OtelMetricProducer} directly.
 */
public final class MetricsOpenTelemetry {

  private MetricsOpenTelemetry() {}

  /**
   * Return a builder for creating an OTLP-backed {@link OpenTelemetrySdk}.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder for OTLP-backed {@link OpenTelemetrySdk}.
   */
  public static final class Builder {

    private static final String DEFAULT_ENDPOINT = "http://localhost:4317";
    private static final String DEFAULT_SCOPE = "io.avaje.metrics";
    private static final Duration DEFAULT_METER_INTERVAL = Duration.ofSeconds(60);
    private static final Duration DEFAULT_TRACE_INTERVAL = Duration.ofSeconds(10);
    private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");

    private boolean includeTrace = true;
    private boolean includeMeter = true;
    private String endpoint = DEFAULT_ENDPOINT;
    private String serviceName;
    private long timedThresholdMicros;
    private Duration meterInterval = DEFAULT_METER_INTERVAL;
    private Duration traceInterval = DEFAULT_TRACE_INTERVAL;
    private MetricRegistry registry;
    private final Map<String, String> resourceAttributes = new LinkedHashMap<>();
    private ContextPropagators propagators = ContextPropagators.create(W3CTraceContextPropagator.getInstance());
    private MetricExporter metricExporter;
    private MetricReader metricReader;
    private SpanExporter spanExporter;

    /**
     * Set the OTLP endpoint used for both metrics and traces.
     *
     * <p>Default is {@code http://localhost:4317}.
     *
     * @param endpoint the OTLP endpoint
     * @return this builder
     */
    public Builder endpoint(String endpoint) {
      this.endpoint = requireNonNull(endpoint);
      return this;
    }

    /**
     * Set the {@code service.name} resource attribute.
     *
     * <p>Defaults to {@code otel.service.name}, then {@code OTEL_SERVICE_NAME}, then the
     * OpenTelemetry SDK default {@code unknown_service:java}.
     *
     * @param serviceName the service name
     * @return this builder
     */
    public Builder serviceName(String serviceName) {
      this.serviceName = requireNonNull(serviceName);
      return this;
    }

    /**
     * Set an OpenTelemetry resource attribute.
     *
     * @param key the resource attribute key
     * @param value the resource attribute value
     * @return this builder
     */
    public Builder resourceAttribute(String key, String value) {
      ResourceAttributes.put(resourceAttributes, key, value);
      return this;
    }

    /**
     * Set OpenTelemetry resource attributes.
     *
     * @param attributes the resource attributes to add
     * @return this builder
     */
    public Builder resourceAttributes(Map<String, String> attributes) {
      requireNonNull(attributes, "attributes").forEach(this::resourceAttribute);
      return this;
    }

    /**
     * Set OpenTelemetry resource attributes from a comma-separated {@code key=value} string.
     *
     * @param attributes the resource attributes string
     * @return this builder
     */
    public Builder resourceAttributes(String attributes) {
      resourceAttributes.putAll(ResourceAttributes.parse(attributes));
      return this;
    }

    /**
     * Set the {@code deployment.environment.name} resource attribute.
     *
     * @param value the deployment environment name
     * @return this builder
     */
    public Builder deploymentEnvironmentName(String value) {
      ResourceAttributes.put(
        resourceAttributes,
        ResourceAttributes.DEPLOYMENT_ENVIRONMENT_NAME,
        value);
      return this;
    }

    /**
     * Set the {@code system.namespace} resource attribute.
     *
     * @param value the system namespace
     * @return this builder
     */
    public Builder systemNamespace(String value) {
      ResourceAttributes.put(
        resourceAttributes,
        ResourceAttributes.SYSTEM_NAMESPACE,
        value);
      return this;
    }

    /**
     * Set whether trace export is included.
     *
     * <p>Default is {@code true}.
     *
     * @param includeTrace when true include trace export
     * @return this builder
     */
    public Builder includeTrace(boolean includeTrace) {
      this.includeTrace = includeTrace;
      return this;
    }

    /**
     * Set whether metric export is included.
     *
     * <p>Default is {@code true}.
     *
     * @param includeMeter when true include metric export
     * @return this builder
     */
    public Builder includeMeter(boolean includeMeter) {
      this.includeMeter = includeMeter;
      return this;
    }

    /**
     * Set the timer total threshold, in microseconds, passed to {@link OtelMetricProducer}.
     *
     * <p>Default is {@code 0}, meaning all timers are reported.
     *
     * @param timedThresholdMicros the minimum timer total to export
     * @return this builder
     */
    public Builder timedThresholdMicros(long timedThresholdMicros) {
      this.timedThresholdMicros = timedThresholdMicros;
      return this;
    }

    /**
     * Set the OTLP metrics export interval.
     *
     * <p>Default is 60 seconds.
     *
     * @param meterInterval the metrics export interval
     * @return this builder
     */
    public Builder meterInterval(Duration meterInterval) {
      this.meterInterval = requireNonNull(meterInterval);
      return this;
    }

    /**
     * Set the trace batch export schedule delay.
     *
     * <p>Default is 10 seconds.
     *
     * @param traceInterval the trace export interval
     * @return this builder
     */
    public Builder traceInterval(Duration traceInterval) {
      this.traceInterval = requireNonNull(traceInterval);
      return this;
    }

    /**
     * Set the {@link MetricRegistry} used by {@link OtelMetricProducer}.
     *
     * <p>Defaults to {@code Metrics.registry()}.
     *
     * @param registry the registry to export
     * @return this builder
     */
    public Builder registry(MetricRegistry registry) {
      this.registry = requireNonNull(registry);
      return this;
    }

    /**
     * Set the context propagators to use.
     *
     * <p>Default is W3C trace context.
     *
     * @param propagators the propagators
     * @return this builder
     */
    public Builder propagators(ContextPropagators propagators) {
      this.propagators = requireNonNull(propagators);
      return this;
    }

    /**
     * Set the metric exporter used by the periodic metric reader.
     *
     * <p>Defaults to an {@link OtlpGrpcMetricExporter} built from the configured endpoint.
     *
     * @param metricExporter the metric exporter
     * @return this builder
     */
    public Builder metricExporter(MetricExporter metricExporter) {
      this.metricExporter = requireNonNull(metricExporter);
      return this;
    }

    /**
     * Set the span exporter used by the batch span processor.
     *
     * <p>Defaults to an {@link OtlpGrpcSpanExporter} built from the configured endpoint.
     *
     * @param spanExporter the span exporter
     * @return this builder
     */
    public Builder spanExporter(SpanExporter spanExporter) {
      this.spanExporter = requireNonNull(spanExporter);
      return this;
    }

    /**
     * Build and return an {@link OpenTelemetrySdk}.
     *
     * @return the built SDK
     */
    public OpenTelemetrySdk build() {
      return sdkBuilder().build();
    }

    /**
     * Build the SDK and register it as the global OpenTelemetry instance.
     *
     * @return the built and globally registered SDK
     */
    public OpenTelemetrySdk buildAndRegisterGlobal() {
      return sdkBuilder().buildAndRegisterGlobal();
    }

    Builder metricReader(MetricReader metricReader) {
      this.metricReader = requireNonNull(metricReader, "metricReader");
      return this;
    }

    private OpenTelemetrySdkBuilder sdkBuilder() {
      var builder = OpenTelemetrySdk.builder()
        .setPropagators(propagators);
      var resource = resource();
      if (includeTrace) {
        builder.setTracerProvider(tracerProvider(resource));
      }
      if (includeMeter) {
        builder.setMeterProvider(meterProvider(resource));
      }
      return builder;
    }

    private Resource resource() {
      var attributes = new LinkedHashMap<String, String>();
      attributes.putAll(ResourceAttributes.configuredAttributes());
      attributes.putAll(resourceAttributes);
      var resource = Resource.getDefault();
      if (!attributes.isEmpty()) {
        resource = resource.merge(ResourceAttributes.resource(attributes));
      }
      var configuredServiceName = ResourceAttributes.configuredServiceName();
      if (configuredServiceName != null) {
        resource = resource.merge(Resource.create(Attributes.of(SERVICE_NAME, configuredServiceName)));
      }
      if (serviceName != null) {
        resource = resource.merge(Resource.create(Attributes.of(SERVICE_NAME, serviceName)));
      }
      return resource;
    }

    private SdkMeterProvider meterProvider(Resource resource) {
      var effectiveRegistry = registry != null ? registry : Metrics.registry();
      var producer = OtelMetricProducer.builder()
        .registry(effectiveRegistry)
        .scopeName(DEFAULT_SCOPE)
        .timedThresholdMicros(timedThresholdMicros)
        .build();

      return SdkMeterProvider.builder()
        .setResource(resource)
        .registerMetricReader(metricReader())
        .registerMetricProducer(producer)
        .build();
    }

    private SdkTracerProvider tracerProvider(Resource resource) {
      var batchProcessor = BatchSpanProcessor.builder(spanExporter())
        .setScheduleDelay(traceInterval)
        .build();

      return SdkTracerProvider.builder()
        .setResource(resource)
        .addSpanProcessor(batchProcessor)
        .build();
    }

    private MetricReader metricReader() {
      if (metricReader != null) {
        return metricReader;
      }
      return PeriodicMetricReader.builder(
        metricExporter())
        .setInterval(meterInterval)
        .build();
    }

    private MetricExporter metricExporter() {
      if (metricExporter != null) {
        return metricExporter;
      }
      return OtlpGrpcMetricExporter.builder()
        .setEndpoint(endpoint)
        .build();
    }

    private SpanExporter spanExporter() {
      if (spanExporter != null) {
        return spanExporter;
      }
      return OtlpGrpcSpanExporter.builder()
        .setEndpoint(endpoint)
        .build();
    }
  }
}
