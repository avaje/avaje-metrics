package org.avaje.metric;

public interface MetricVisitor {

  /**
   * Start visiting a metric.
   */
  public void visitBegin(Metric metric);
  
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
  public void visit(Stats.MovingSummary summary);
  
  /**
   * End of visiting a metric.
   */
  public void visitEnd(Metric metric);
  
}
