package io.avaje.metrics;

import java.util.Collection;

/**
 * Typically used for reporting metrics.
 */
public interface MetricStatsVisitor {

  /**
   * Visit TimedStatistics.
   */
  void visit(TimedMetric.Stats timed);

  /**
   * Visit ValueStatistics.
   */
  void visit(ValueMetric.Stats value);

  /**
   * Visit CounterStatistics.
   */
  void visit(CounterMetric.Stats counter);

  /**
   * Visit GaugeDoubleStatistics
   */
  void visit(GaugeDoubleMetric.Stats gauge);

  /**
   * Visit GaugeLongStatistics.
   */
  void visit(GaugeLongMetric.Stats gauge);

  /**
   * Visit all the metrics.
   */
  default void visitAll(Collection<MetricStats> all) {
    for (MetricStats stats : all) {
      stats.visit(this);
    }
  }
}
