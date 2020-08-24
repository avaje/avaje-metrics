package io.avaje.metrics;

import io.avaje.metrics.statistics.MetricStatisticsVisitor;

/**
 * A Metric that collects statistics on events.
 * <ul>
 * <li>TimedMetric and BucketTimedMetric are used for monitoring execution time</li>
 * <li>CounterMetric is for counting discrete events like 'user logged in'</li>
 * <li>ValueMetric is used when events have a value like bytes sent, lines read</li>
 * <li>Gauges measure the current value of a resource like 'used memory' or 'active threads'.</li>
 * </ul>
 */
public interface Metric {

  /**
   * Return the name of the metric.
   */
  MetricName getName();

  /**
   * Typically this is only called by the MetricManager and tells the metric to collect its underlying statistics for
   * reporting purposes and in addition resetting and internal counters it has.
   */
  void collect(MetricStatisticsVisitor collector);

  /**
   * Clear the statistics resetting any internal counters etc.
   * <p>
   * Typically the MetricManager takes care of resetting the statistic/counters for the metrics when
   * it periodically collects and reports all the metrics and you are not expected to use this method.
   * </p>
   */
  void clear();

}
