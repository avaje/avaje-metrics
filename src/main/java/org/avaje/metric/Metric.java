package org.avaje.metric;

/**
 * A Metric which collects statistics on events.
 */
public interface Metric {

  /**
   * Return the name of the metric.
   */
  public MetricName getName();

  /**
   * Clear the statistics.
   */
  public void clearStatistics();

  /**
   * Force the statistics to be updated.
   * <p>
   * Typically events are queued and the statistics are calculated in a
   * background thread automatically. This forces the statistics to be
   * calculated.
   * </p>
   */
  public void updateStatistics();
}
