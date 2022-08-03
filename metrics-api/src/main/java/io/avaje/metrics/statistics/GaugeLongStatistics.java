package io.avaje.metrics.statistics;

import io.avaje.metrics.GaugeLongMetric;

/**
 * Statistics provided by the {@link GaugeLongMetric}.
 */
public interface GaugeLongStatistics extends MetricStatistics {

  /**
   * Return the count of values collected.
   */
  long getValue();
}
