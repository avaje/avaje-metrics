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
   * Collect process-lifetime values without resetting the underlying counters.
   */
  CUMULATIVE
}
