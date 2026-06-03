package io.avaje.metrics.otel;

import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.Metrics;
import io.avaje.metrics.otel.producer.OtelMetricProducer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
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
import io.opentelemetry.sdk.trace.samplers.Sampler;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
   * OTLP exporter protocol used by the default metric and span exporters.
   */
  public enum Protocol {

    /**
     * OTLP gRPC exporter protocol.
     */
    GRPC("http://localhost:4317"),

    /**
     * OTLP HTTP/protobuf exporter protocol.
     */
    HTTP_PROTOBUF("http://localhost:4318");

    private final String defaultEndpoint;

    Protocol(String defaultEndpoint) {
      this.defaultEndpoint = defaultEndpoint;
    }

    String defaultEndpoint() {
      return defaultEndpoint;
    }
  }

  /**
   * Builder for OTLP-backed {@link OpenTelemetrySdk}.
   */
  public static final class Builder {

    private static final String DEFAULT_SCOPE = "io.avaje.metrics";
    private static final Duration DEFAULT_METER_INTERVAL = Duration.ofSeconds(60);
    private static final Duration DEFAULT_METER_INTERVAL_LAMBDA = Duration.ofSeconds(30);
    private static final Duration DEFAULT_TRACE_INTERVAL = Duration.ofSeconds(10);
    private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");
    private static final String METRICS_PATH = "/v1/metrics";
    private static final String TRACES_PATH = "/v1/traces";

    private boolean includeTrace = true;
    private boolean includeMeter = true;
    private Protocol protocol = Protocol.GRPC;
    private String endpoint;
    private String serviceName;
    private long timedThresholdMicros;
    private Duration meterInterval;
    private Duration traceInterval;
    private MetricRegistry registry;
    private final Map<String, String> resourceAttributes = new LinkedHashMap<>();
    private ContextPropagators propagators = ContextPropagators.create(W3CTraceContextPropagator.getInstance());
    private MetricExporter metricExporter;
    private MetricReader metricReader;
    private SpanExporter spanExporter;
    private Sampler sampler;
    private Double traceSampleRatio;
    private Duration connectTimeout;
    private Duration exportTimeout;

    /**
     * Set the OTLP endpoint used for both metrics and traces.
     *
     * <p>For {@link Protocol#GRPC}, the endpoint is passed directly to the default gRPC exporters.
     * Default is {@code http://localhost:4317}.
     *
     * <p>For {@link Protocol#HTTP_PROTOBUF}, the endpoint is a base endpoint. Default is
     * {@code http://localhost:4318}; the builder appends {@code /v1/metrics} or
     * {@code /v1/traces} for the default HTTP exporters.
     *
     * @param endpoint the OTLP endpoint
     * @return this builder
     */
    public Builder endpoint(String endpoint) {
      this.endpoint = requireNonNull(endpoint);
      return this;
    }

    /**
     * Set the OTLP exporter protocol.
     *
     * <p>Default is {@link Protocol#GRPC}. For signal-specific exporter configuration, use
     * {@link #metricExporter(MetricExporter)} and {@link #spanExporter(SpanExporter)}.
     *
     * @param protocol the OTLP exporter protocol
     * @return this builder
     */
    public Builder protocol(Protocol protocol) {
      this.protocol = requireNonNull(protocol);
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
     * Set the {@code service.namespace} resource attribute.
     *
     * @param value the service namespace
     * @return this builder
     */
    public Builder serviceNamespace(String value) {
      ResourceAttributes.put(
        resourceAttributes,
        ResourceAttributes.SERVICE_NAMESPACE,
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
     * Set the sampler used by the tracer provider.
     *
     * <p>This overrides {@link #traceSampleRatio(double)} and standard OpenTelemetry sampler
     * configuration.
     *
     * @param sampler the sampler to use
     * @return this builder
     */
    public Builder sampler(Sampler sampler) {
      this.sampler = requireNonNull(sampler);
      return this;
    }

    /**
     * Set the root trace sample ratio from {@code 0.0} to {@code 1.0}.
     *
     * <p>The effective sampler is {@code parentBased(traceIdRatioBased(ratio))}, so existing
     * upstream sampling decisions are respected while new root traces use the configured ratio.
     *
     * @param ratio the root trace sample ratio
     * @return this builder
     */
    public Builder traceSampleRatio(double ratio) {
      TraceSampling.validateRatio(ratio);
      this.traceSampleRatio = ratio;
      return this;
    }

    /**
     * Set the metric exporter used by the periodic metric reader.
     *
     * <p>Defaults to a protocol-specific OTLP exporter built from the configured endpoint.
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
     * <p>Defaults to a protocol-specific OTLP exporter built from the configured endpoint.
     *
     * @param spanExporter the span exporter
     * @return this builder
     */
    public Builder spanExporter(SpanExporter spanExporter) {
      this.spanExporter = requireNonNull(spanExporter);
      return this;
    }

    /**
     * Set the OTLP exporter connect timeout applied to the default metric and span exporters.
     *
     * <p>Has no effect on exporters supplied via {@link #metricExporter(MetricExporter)} or
     * {@link #spanExporter(SpanExporter)}.
     *
     * @param connectTimeout the connect timeout
     * @return this builder
     */
    public Builder connectTimeout(Duration connectTimeout) {
      this.connectTimeout = requireNonNull(connectTimeout);
      return this;
    }

    /**
     * Set the OTLP exporter request timeout applied to the default metric and span exporters.
     *
     * <p>This is the total time allowed for an export request including the response.
     * Has no effect on exporters supplied via {@link #metricExporter(MetricExporter)} or
     * {@link #spanExporter(SpanExporter)}.
     *
     * @param exportTimeout the export request timeout
     * @return this builder
     */
    public Builder exportTimeout(Duration exportTimeout) {
      this.exportTimeout = requireNonNull(exportTimeout);
      return this;
    }

    /**
     * Enable the {@code waitIfRunning} pattern for use with AWS Lambda or similar
     * "freeze-on-exit" runtimes.
     *
     * <p>The metric and span exporters are wrapped to track in-flight exports.
     * Background reporting continues on its normal schedule (no per-invocation flush);
     * the returned {@link TelemetryWaiter} blocks at the end of an invocation only if a
     * scheduled background export is currently in progress.
     *
     * <p>This is the last call before {@code build()} - the returned {@link WaiterBuilder}
     * exposes only the build methods, {@link WaiterBuilder#timeout(long, TimeUnit)} and
     * {@link WaiterBuilder#flushIfStale(Duration)}.
     *
     * <p>Calling this method also flips two defaults to Lambda-friendly values, but only
     * when the caller has not already set them explicitly:
     * <ul>
     *   <li>{@code meterInterval} → 30 seconds (was 60 seconds)</li>
     *   <li>{@code flushIfStale} → {@code 2 × meterInterval} (so 60 seconds by default)</li>
     * </ul>
     *
     * @return a builder that produces an SDK paired with a {@link TelemetryWaiter}
     */
    public WaiterBuilder enableWaitIfRunning() {
      return new WaiterBuilder(this);
    }

    /**
     * Build and return an {@link OpenTelemetrySdk}.
     *
     * @return the built SDK
     */
    public OpenTelemetrySdk build() {
      resolveIntervals(false);
      return sdkBuilder().build();
    }

    /**
     * Build the SDK and register it as the global OpenTelemetry instance.
     *
     * @return the built and globally registered SDK
     */
    public OpenTelemetrySdk buildAndRegisterGlobal() {
      resolveIntervals(false);
      return sdkBuilder().buildAndRegisterGlobal();
    }

    /**
     * Apply default intervals (Lambda-friendly when {@code waiting}, otherwise standard)
     * for any interval the caller has not explicitly set.
     */
    void resolveIntervals(boolean waiting) {
      if (meterInterval == null) {
        meterInterval = waiting ? DEFAULT_METER_INTERVAL_LAMBDA : DEFAULT_METER_INTERVAL;
      }
      if (traceInterval == null) {
        traceInterval = DEFAULT_TRACE_INTERVAL;
      }
    }

    Builder metricReader(MetricReader metricReader) {
      this.metricReader = requireNonNull(metricReader, "metricReader");
      return this;
    }

    String metricExporterEndpoint() {
      if (protocol == Protocol.HTTP_PROTOBUF) {
        return otlpHttpEndpoint(endpoint(), METRICS_PATH);
      }
      return endpoint();
    }

    String spanExporterEndpoint() {
      if (protocol == Protocol.HTTP_PROTOBUF) {
        return otlpHttpEndpoint(endpoint(), TRACES_PATH);
      }
      return endpoint();
    }

    static String otlpHttpEndpoint(String endpoint, String signalPath) {
      var baseEndpoint = endpoint.trim();
      while (baseEndpoint.endsWith("/")) {
        baseEndpoint = baseEndpoint.substring(0, baseEndpoint.length() - 1);
      }
      return baseEndpoint + signalPath;
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

      var builder = SdkTracerProvider.builder()
        .setResource(resource)
        .addSpanProcessor(batchProcessor);
      var sampler = effectiveSampler();
      if (sampler != null) {
        builder.setSampler(sampler);
      }
      return builder.build();
    }

    private Sampler effectiveSampler() {
      if (sampler != null) {
        return sampler;
      }
      if (traceSampleRatio != null) {
        return TraceSampling.parentBasedTraceIdRatio(traceSampleRatio);
      }
      return TraceSampling.configuredSampler();
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

    MetricExporter metricExporter() {
      if (metricExporter != null) {
        return metricExporter;
      }
      if (protocol == Protocol.HTTP_PROTOBUF) {
        var b = OtlpHttpMetricExporter.builder()
          .setEndpoint(metricExporterEndpoint());
        if (connectTimeout != null) b.setConnectTimeout(connectTimeout);
        if (exportTimeout != null) b.setTimeout(exportTimeout);
        return b.build();
      }
      var b = OtlpGrpcMetricExporter.builder()
        .setEndpoint(metricExporterEndpoint());
      if (connectTimeout != null) b.setConnectTimeout(connectTimeout);
      if (exportTimeout != null) b.setTimeout(exportTimeout);
      return b.build();
    }

    SpanExporter spanExporter() {
      if (spanExporter != null) {
        return spanExporter;
      }
      if (protocol == Protocol.HTTP_PROTOBUF) {
        var b = OtlpHttpSpanExporter.builder()
          .setEndpoint(spanExporterEndpoint());
        if (connectTimeout != null) b.setConnectTimeout(connectTimeout);
        if (exportTimeout != null) b.setTimeout(exportTimeout);
        return b.build();
      }
      var b = OtlpGrpcSpanExporter.builder()
        .setEndpoint(spanExporterEndpoint());
      if (connectTimeout != null) b.setConnectTimeout(connectTimeout);
      if (exportTimeout != null) b.setTimeout(exportTimeout);
      return b.build();
    }

    boolean includeMeter() {
      return includeMeter;
    }

    boolean includeTrace() {
      return includeTrace;
    }

    Duration meterInterval() {
      return meterInterval;
    }

    void setMetricExporterField(MetricExporter exporter) {
      this.metricExporter = exporter;
    }

    void setSpanExporterField(SpanExporter exporter) {
      this.spanExporter = exporter;
    }

    private String endpoint() {
      return endpoint != null ? endpoint : protocol.defaultEndpoint();
    }
  }

  /**
   * Result of building an SDK with {@link Builder#enableWaitIfRunning()}, pairing the
   * {@link OpenTelemetrySdk} with a {@link TelemetryWaiter} that can be used at the end
   * of a Lambda invocation to wait for any in-flight background export to complete.
   */
  public static final class Result {

    private final OpenTelemetrySdk sdk;
    private final TelemetryWaiter waiter;

    Result(OpenTelemetrySdk sdk, TelemetryWaiter waiter) {
      this.sdk = sdk;
      this.waiter = waiter;
    }

    /** The built OpenTelemetry SDK. */
    public OpenTelemetrySdk sdk() {
      return sdk;
    }

    /** The waiter coordinating in-flight exports. */
    public TelemetryWaiter waiter() {
      return waiter;
    }
  }

  /**
   * Builder produced by {@link Builder#enableWaitIfRunning()} that builds an
   * {@link OpenTelemetrySdk} together with a {@link TelemetryWaiter}.
   *
   * <p>The configured (or default) metric and span exporters are wrapped so the
   * waiter can observe in-flight background exports without triggering an additional
   * export. Background reporting continues on its normal schedule.
   */
  public static final class WaiterBuilder {

    private static final long DEFAULT_TIMEOUT_MILLIS = 5_000L;

    private final Builder builder;
    private long timeoutMillis = DEFAULT_TIMEOUT_MILLIS;
    private Duration flushIfStale;

    WaiterBuilder(Builder builder) {
      this.builder = builder;
    }

    /**
     * Set the default timeout used by {@link TelemetryWaiter#waitIfRunning()}.
     *
     * <p>Default is 5 seconds. The timeout applies independently per signal
     * (metrics, traces) and to any subsequent stale {@code forceFlush()}.
     *
     * @param timeout the wait timeout
     * @param unit the time unit
     * @return this builder
     */
    public WaiterBuilder timeout(long timeout, TimeUnit unit) {
      this.timeoutMillis = unit.toMillis(timeout);
      return this;
    }

    /**
     * Set the stale threshold that triggers a synchronous {@code forceFlush()} at the end
     * of {@link TelemetryWaiter#waitIfRunning()} when no successful background export has
     * completed within the threshold.
     *
     * <p>This is intended for low-traffic Lambda environments where the periodic metric
     * reader is frozen between invocations and may not tick before the runtime is
     * suspended again. In busy environments {@code lastSuccess} stays fresh and
     * forceFlush is a no-op.
     *
     * <p>Default is {@code 2 × meterInterval}, so a healthy reader keeps {@code lastSuccess}
     * younger than the threshold; one missed tick is tolerated; two or more missed ticks
     * trigger a flush. Pass {@link Duration#ZERO} to disable.
     *
     * @param flushIfStale the stale threshold
     * @return this builder
     */
    public WaiterBuilder flushIfStale(Duration flushIfStale) {
      this.flushIfStale = requireNonNull(flushIfStale);
      return this;
    }

    /**
     * Build the {@link OpenTelemetrySdk} paired with a {@link TelemetryWaiter}.
     *
     * @return the result containing both the SDK and the waiter
     */
    public Result build() {
      return buildResult(false);
    }

    /**
     * Build the SDK, register it as the global OpenTelemetry instance, and return it
     * paired with a {@link TelemetryWaiter}.
     *
     * @return the result containing both the SDK and the waiter
     */
    public Result buildAndRegisterGlobal() {
      return buildResult(true);
    }

    private Result buildResult(boolean registerGlobal) {
      builder.resolveIntervals(true);
      WaitingMetricExporter waitingMetric = null;
      WaitingSpanExporter waitingSpan = null;
      if (builder.includeMeter()) {
        waitingMetric = new WaitingMetricExporter(builder.metricExporter());
        builder.setMetricExporterField(waitingMetric);
      }
      if (builder.includeTrace()) {
        waitingSpan = new WaitingSpanExporter(builder.spanExporter());
        builder.setSpanExporterField(waitingSpan);
      }
      var sdk = registerGlobal ? builder.buildAndRegisterGlobal() : builder.build();
      var waiter = new TelemetryWaiter(waitingMetric, waitingSpan, sdk, timeoutMillis, effectiveFlushIfStale());
      return new Result(sdk, waiter);
    }

    private Duration effectiveFlushIfStale() {
      if (flushIfStale != null) {
        return flushIfStale;
      }
      return builder.meterInterval().multipliedBy(2);
    }
  }
}
