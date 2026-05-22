package io.avaje.metrics;

/**
 * Builder used to configure and register a meter.
 *
 * <p>Example:
 *
 * <pre>{@code
 * registry.meterBuilder("app.bytes.sent")
 *   .tags(Tags.of("pod:blue"))
 *   .unit("By")
 *   .build();
 * }</pre>
 */
public interface MeterBuilder {

  /**
   * Set the tags to use for the meter.
   */
  MeterBuilder tags(Tags tags);

  /**
   * Set the unit to use for the meter.
   */
  MeterBuilder unit(String unit);

  /**
   * Create and register the meter.
   */
  Meter build();
}
