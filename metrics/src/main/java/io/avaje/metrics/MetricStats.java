package io.avaje.metrics;

/**
 * Common for statistics of all metrics.
 */
public interface MetricStats {

  /**
   * Return the associated metric name.
   */
  String name();

  /**
   * Visit the reporter for the given metric type.
   */
  void visit(MetricStatsVisitor reporter);

}
