package io.avaje.metrics;

import java.util.Collection;

/**
 * Typically used for reporting metrics.
 */
public interface MetricStatsVisitor {

  /**
   * Visit Timer stats.
   */
  void visit(Timer.Stats timed);

  /**
   * Visit meter stats.
   */
  void visit(Meter.Stats meter);

  /**
   * Visit Counter stats.
   */
  void visit(Counter.Stats counter);

  /**
   * Visit GaugeDouble stats.
   */
  void visit(GaugeDouble.Stats gauge);

  /**
   * Visit GaugeLong stats.
   */
  void visit(GaugeLong.Stats gauge);

  /**
   * Visit all the metrics.
   */
  default void visitAll(Collection<MetricStats> all) {
    for (MetricStats stats : all) {
      stats.visit(this);
    }
  }
}
