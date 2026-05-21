package io.avaje.metrics;

/**
 * The collection mode used when reading metric statistics.
 */
public enum CollectionMode {

  /**
   * Collect the current interval values and reset the underlying counters.
   */
  DELTA,

  /**
   * Collect process-lifetime count and total values without resetting them.
   *
   * <p>Windowed high-water values such as {@code max} may still reset on each collection.
   */
  CUMULATIVE
}
