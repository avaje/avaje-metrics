package org.avaje.metric;

import org.avaje.metric.report.MetricVisitor;


/**
 * A Metric which collects statistics on events.
 */
public interface Metric {

  /**
   * Return the name of the metric.
   */
  public MetricName getName();

  /**
   * Only called by the MetricManager this tells the metric to collect its underlying
   * statistics for reporting purposes reseting internal counters.
   * 
   * @return true if this metric has some values.
   */
  public boolean collectStatistics();
  
  /**
   * Visit the metric typically reading and reporting the underlying statistics.
   */
  public void visitCollectedStatistics(MetricVisitor visitor);

  /**
   * Clear the statistics.
   */
  public void clearStatistics();

}
