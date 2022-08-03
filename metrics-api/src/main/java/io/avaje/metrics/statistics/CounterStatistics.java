package io.avaje.metrics.statistics;

import io.avaje.metrics.CounterMetric;

/**
 * Statistics provided by the {@link CounterMetric}.
 */
public interface CounterStatistics extends MetricStatistics {

  /**
   * Return the count of values collected.
   */
  long getCount();
}
