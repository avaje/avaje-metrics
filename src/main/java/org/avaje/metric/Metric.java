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
   * Visit the metric typically reading and reporting the underlying statistics.
   */
  public void visit(MetricVisitor visitor);

  /**
   * Clear the statistics.
   */
  public void clearStatistics();

}
