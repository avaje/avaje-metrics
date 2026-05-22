package io.avaje.metrics;

import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;

/**
 * Builder used to configure and register a gauge.
 *
 * <p>Example:
 *
 * <pre>{@code
 * registry.gauge("jvm.memory.used")
 *   .tags(Tags.of("pod:blue"))
 *   .unit("MiBy")
 *   .ofLongs(memorySupplier);
 * }</pre>
 */
public interface GaugeBuilder {

  /**
   * Set the tags to use for the gauge.
   */
  GaugeBuilder tags(Tags tags);

  /**
   * Set the unit to use for the gauge.
   */
  GaugeBuilder unit(String unit);

  /**
   * Create and register a long-valued gauge.
   */
  GaugeLong ofLongs(LongSupplier supplier);

  /**
   * Create and register a double-valued gauge.
   */
  GaugeDouble ofDoubles(DoubleSupplier supplier);
}
