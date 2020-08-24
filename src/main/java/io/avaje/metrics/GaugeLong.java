package io.avaje.metrics;

/**
 * A Gauge returning a long value providing the 'source' for a {@link GaugeLongMetric}.
 * <p>
 * A Gauge typically doesn't represent an "Event" but the current value of a resource like threads,
 * memory etc.
 *
 * @see GaugeLongMetric
 */
@FunctionalInterface
public interface GaugeLong {

  /**
   * Return the current value.
   */
  long getValue();

}
