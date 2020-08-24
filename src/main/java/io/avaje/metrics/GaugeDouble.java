package io.avaje.metrics;

/**
 * A Gauge returning a double value providing the 'source' for a {@link GaugeDoubleMetric}.
 * <p>
 * A Gauge typically doesn't represent an "Event" but the current value of a resource like 'active
 * threads' or 'used memory' etc.
 *
 * @see GaugeDoubleMetric
 */
@FunctionalInterface
public interface GaugeDouble {

  /**
   * Return the current value.
   */
  double getValue();

}
