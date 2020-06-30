package io.avaje.metrics.statistics;

/**
 * Typically used for reporting metrics.
 */
public interface MetricStatisticsVisitor {

  /**
   * Visit TimedStatistics.
   */
  void visit(TimedStatistics timed);

  /**
   * Visit ValueStatistics.
   */
  void visit(ValueStatistics value);

  /**
   * Visit CounterStatistics.
   */
  void visit(CounterStatistics counter);

  /**
   * Visit GaugeDoubleStatistics
   */
  void visit(GaugeDoubleStatistics gauge);

  /**
   * Visit GaugeLongStatistics.
   */
  void visit(GaugeLongStatistics gauge);
}
