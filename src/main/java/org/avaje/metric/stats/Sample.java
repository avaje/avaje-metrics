package org.avaje.metric.stats;

import org.avaje.metric.MetricValueEvent;
import org.avaje.metric.Stats;

/**
 * A statistically representative sample of a data stream.
 */
interface Sample {
  /**
   * Clears all recorded values.
   */
  void clear();

  /**
   * Returns the number of values recorded.
   * 
   * @return the number of values recorded
   */
  int size();

  /**
   * Adds a new recorded value to the sample.
   * 
   * @param value
   *          a new recorded value
   */
  void update(MetricValueEvent event);// long value);

  /**
   * Returns a snapshot of the sample's values.
   * 
   * @return a snapshot of the sample's values
   */
  Stats.Percentiles getSnapshot();
}
