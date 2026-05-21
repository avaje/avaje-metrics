package io.avaje.metrics.otel.reporter;

import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.Metrics;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;

import java.util.concurrent.TimeUnit;

final class DOtelBuilder implements OtelReporter.Builder {

  private static final String DEFAULT_SCOPE = "io.avaje.metrics";

  private OpenTelemetry openTelemetry;
  private MeterProvider meterProvider;
  private MetricRegistry registry;
  private int schedule = 60;
  private TimeUnit scheduleTimeUnit = TimeUnit.SECONDS;
  private long timedThresholdMicros = 0;
  private String scopeName = DEFAULT_SCOPE;

  @Override
  public OtelReporter.Builder openTelemetry(OpenTelemetry openTelemetry) {
    this.openTelemetry = openTelemetry;
    return this;
  }

  @Override
  public OtelReporter.Builder meterProvider(MeterProvider meterProvider) {
    this.meterProvider = meterProvider;
    return this;
  }

  @Override
  public OtelReporter.Builder registry(MetricRegistry registry) {
    this.registry = registry;
    return this;
  }

  @Override
  public OtelReporter.Builder schedule(int schedule, TimeUnit timeUnit) {
    this.schedule = schedule;
    this.scheduleTimeUnit = timeUnit;
    return this;
  }

  @Override
  public OtelReporter.Builder timedThresholdMicros(long threshold) {
    this.timedThresholdMicros = threshold;
    return this;
  }

  @Override
  public OtelReporter.Builder scopeName(String scopeName) {
    this.scopeName = scopeName;
    return this;
  }

  @Override
  public OtelReporter build() {
    MetricRegistry effectiveRegistry = registry != null ? registry : Metrics.registry();
    Meter otelMeter = resolveMeter();
    OtelVisitor visitor = new OtelVisitor(otelMeter, timedThresholdMicros);
    return new DOtelReporter(effectiveRegistry, visitor, schedule, scheduleTimeUnit);
  }

  private Meter resolveMeter() {
    MeterProvider provider = resolveProvider();
    return provider.meterBuilder(scopeName).build();
  }

  private MeterProvider resolveProvider() {
    if (meterProvider != null) {
      return meterProvider;
    }
    if (openTelemetry != null) {
      return openTelemetry.getMeterProvider();
    }
    return OpenTelemetry.noop().getMeterProvider();
  }
}
