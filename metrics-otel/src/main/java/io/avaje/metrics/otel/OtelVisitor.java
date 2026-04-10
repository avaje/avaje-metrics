package io.avaje.metrics.otel;

import io.avaje.metrics.*;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleGauge;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongGauge;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Implements {@link Metric.Visitor} to map avaje metric statistics to OpenTelemetry instruments.
 * <p>
 * Instruments are created lazily on the first visit of each metric name and cached for reuse
 * on subsequent reporting cycles.
 * <p>
 * Mapping:
 * <ul>
 *   <li>{@link Counter.Stats} → {@code LongCounter} (add delta count)</li>
 *   <li>{@link Timer.Stats} → three instruments: {@code .count} (LongCounter),
 *       {@code .total} (LongCounter, unit {@code us}), {@code .max} (LongGauge, unit {@code us})</li>
 *   <li>{@link Meter.Stats} → same three instruments as Timer, unit {@code 1}</li>
 *   <li>{@link GaugeLong.Stats} → {@code LongGauge} (set current value)</li>
 *   <li>{@link GaugeDouble.Stats} → {@code DoubleGauge} (set current value)</li>
 * </ul>
 * Timer success and error stats are naturally handled as separate metrics because avaje
 * emits them with different names ({@code name} and {@code name.error}).
 */
final class OtelVisitor implements Metric.Visitor {

  private final io.opentelemetry.api.metrics.Meter otelMeter;
  private final long timedThresholdMicros;

  private final ConcurrentHashMap<String, LongCounter> counterCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, LongCounter> meterCountCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, LongCounter> meterTotalCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, LongGauge> meterMaxCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, LongGauge> gaugeLongCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, DoubleGauge> gaugeDoubleCache = new ConcurrentHashMap<>();

  OtelVisitor(io.opentelemetry.api.metrics.Meter otelMeter, long timedThresholdMicros) {
    this.otelMeter = otelMeter;
    this.timedThresholdMicros = timedThresholdMicros;
  }

  @Override
  public void visit(Counter.Stats stats) {
    if (stats.count() == 0) {
      return;
    }
    Attributes attrs = OtelAttributes.of(stats.tags());
    getOrCreate(counterCache, stats.name(), name ->
        otelMeter.counterBuilder(name)
            .setUnit("{event}")
            .build()
    ).add(stats.count(), attrs);
  }

  @Override
  public void visit(Timer.Stats stats) {
    if (timedThresholdMicros > 0 && stats.total() < timedThresholdMicros) {
      return;
    }
    if (stats.count() == 0) {
      return;
    }
    recordMeterStats(stats, "us");
  }

  @Override
  public void visit(Meter.Stats stats) {
    if (stats.count() == 0) {
      return;
    }
    recordMeterStats(stats, "1");
  }

  @Override
  public void visit(GaugeDouble.Stats stats) {
    Attributes attrs = OtelAttributes.of(stats.tags());
    getOrCreate(gaugeDoubleCache, stats.name(), name ->
        otelMeter.gaugeBuilder(name)
            .setUnit("1")
            .build()
    ).set(stats.value(), attrs);
  }

  @Override
  public void visit(GaugeLong.Stats stats) {
    Attributes attrs = OtelAttributes.of(stats.tags());
    getOrCreate(gaugeLongCache, stats.name(), name ->
        otelMeter.gaugeBuilder(name)
            .ofLongs()
            .setUnit("1")
            .build()
    ).set(stats.value(), attrs);
  }

  private void recordMeterStats(Meter.Stats stats, String unit) {
    Attributes attrs = OtelAttributes.of(stats.tags());
    String name = stats.name();

    getOrCreate(meterCountCache, name, n ->
        otelMeter.counterBuilder(n + ".count")
            .setUnit("{event}")
            .build()
    ).add(stats.count(), attrs);

    getOrCreate(meterTotalCache, name, n ->
        otelMeter.counterBuilder(n + ".total")
            .setUnit(unit)
            .build()
    ).add(stats.total(), attrs);

    getOrCreate(meterMaxCache, name, n ->
        otelMeter.gaugeBuilder(n + ".max")
            .ofLongs()
            .setUnit(unit)
            .build()
    ).set(stats.max(), attrs);
  }

  private <T> T getOrCreate(ConcurrentHashMap<String, T> cache, String name, Function<String, T> factory) {
    return cache.computeIfAbsent(name, factory);
  }
}
