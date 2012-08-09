package org.avaje.metric;

/**
 * Visitor for the statistics held by a metric.
 */
public interface MetricVisitor {

  public int getCollectionRateSeconds();
  
  /**
   * Return true if summary statistics should be reset after being visited.
   */
  public boolean isResetSummaryStatistics();

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
   * Visit the moving average for the rate of events occurring.
   */
  public void visitEventRate(Stats.MovingAverages eventRate);

  /**
   * Visit the moving average for the rate of load/work occurring.
   */
  public void visitLoadRate(Stats.MovingAverages loadRate);

  /**
   * Visit an underlying summary statistics.
   */
  public void visit(Stats.Summary summary);

  /**
   * End of visiting a metric.
   */
  public void visitEnd(Metric metric);

}
