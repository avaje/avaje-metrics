package org.avaje.metric;

import java.util.concurrent.TimeUnit;

/**
 * A Metric which collects statistics on events.
 */
public interface Metric {

  /**
   * Return the name of the metric.
   */
  public MetricName getName();

  /**
   * Return the TimeUnit used to measure the rate at which events occur.
   * <p>
   * An appropriate TimeUnit is specified for each metric so that the reported
   * statistics are to a reasonable scale (events per millisecond, events per
   * second, events per minute, events per hour etc).
   * </p>
   */
  public TimeUnit getRateTimeUnit();
  
  /**
   * Return the abbreviation for the rate time unit.
   */
  public String getRateUnitAbbreviation();

  /**
   * Visit the metric typically reading and reporting the underlying statistics.
   */
  public void visit(MetricVisitor visitor);

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
