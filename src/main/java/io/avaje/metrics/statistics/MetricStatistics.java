package io.avaje.metrics.statistics;

/**
 * Common for statistics of all metrics.
 */
public interface MetricStatistics {

  /**
   * Return the associated metric name.
   */
  String getName();

  /**
   * Visit the reporter for the given metric type.
   */
  void visit(MetricStatisticsVisitor reporter);

}
