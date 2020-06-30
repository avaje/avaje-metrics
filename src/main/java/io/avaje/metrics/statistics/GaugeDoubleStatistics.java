package io.avaje.metrics.statistics;

import io.avaje.metrics.GaugeDoubleMetric;

/**
 * Statistics provided by the {@link GaugeDoubleMetric}.
 */
public interface GaugeDoubleStatistics extends MetricStatistics {

  /**
   * Return the time the counter started statistics collection.
   */
  long getStartTime();

  /**
   * Return the count of values collected.
   */
  double getValue();
}
