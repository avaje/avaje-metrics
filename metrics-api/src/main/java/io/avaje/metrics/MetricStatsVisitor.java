package io.avaje.metrics;

import java.util.Collection;
import java.util.function.Function;

/**
 * Typically used for reporting metrics.
 */
public interface MetricStatsVisitor {

  /**
   * Return the naming convention to use when reporting metrics.
   */
  default Function<String, String> namingConvention() {
    return NamingMatch.INSTANCE;
  }

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
