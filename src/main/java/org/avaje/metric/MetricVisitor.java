package org.avaje.metric;

/**
 * Visitor for the statistics held by a metric.
 */
public interface MetricVisitor {

  /**
   * Return the rate in seconds the statistics are collected.
   */
  public int getCollectionRateSeconds();
  
  /**
   * Return true if statistics should be reset after being visited.
   */
  public boolean isResetStatistics();

  /**
   * Start visiting a metric with a flag indicating if the metric currently has
   * any statistics.
   * <p>
   * Return true if the metric should be visited. This provides a way to skip
   * visiting empty metrics during reporting.
   * </p>
   * 
   * @return true if the metric should be visited
   */
  public boolean visitBegin(Metric metric, boolean emptyMetric);

  /**
   * Start visiting error statistics.
   * <p>
   * All MovingAverages and Summary statistics after this are for error events.
   * </p>
   */
  public void visitErrorsBegin();

  /**
   * Finished visiting the error statistics.
   */
  public void visitErrorsEnd();

  /**
   * Visit the load statistics.
   */
  public void visit(LoadStatistics loadStatistics);
  
  /**
   * Visit the counter statistics.
   */
  public void visit(CounterStatistics counterStatistics);

  /**
   * Visit value statistics.
   */
  public void visit(ValueStatistics valueStatistics);

  /**
   * Visit a GaugeMetricGroup.
   */
  public void visit(GaugeMetricGroup gaugeMetricGroup);

  /**
   * Visit an individual GaugeMetric.
   */
  public void visit(GaugeMetric gaugeMetric);

  
  /**
   * End of visiting a metric.
   */
  public void visitEnd(Metric metric);

}
