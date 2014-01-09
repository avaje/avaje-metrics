package org.avaje.metric;


/**
 * Designed to capture the duration of timed events.
 * <p>
 * The major difference compared with ValueMetric is that it is specifically
 * oriented towards collecting time duration and provides separate statistics
 * for success and error completion.
 * </p>
 */
public final class TimedMetric implements Metric {

  private final MetricName name;

  private final ValueCounter successCounter = new ValueCounter(true);
  
  private final ValueCounter errorCounter = new ValueCounter(true);
  
  public TimedMetric(MetricName name) {
    this.name = name;
  }

  public String toString() {
    return name.toString();
  }

  public ValueStatistics getSuccessStatistics(boolean reset) {
    return successCounter.getStatistics(reset);
  }
  
  public ValueStatistics getErrorStatistics(boolean reset) {
    return errorCounter.getStatistics(reset);
  }
  
  @Override
  public void clearStatistics() {
    successCounter.reset();
    errorCounter.reset();
  }

  protected long getTimeMillis() {
    return System.currentTimeMillis();
  }

  protected long getTickNanos() {
    return System.nanoTime();
  }

  public void visit(MetricVisitor visitor) {
    
    boolean emptyMetric = successCounter.isEmpty() && errorCounter.isEmpty();
    if (!visitor.visitBegin(this, emptyMetric)) {
      // skip processing this metric
      if (emptyMetric) {
        // reset effectively moving the resetStartTime to now
        successCounter.reset();
        errorCounter.reset();
      }
      
    } else {
      visitor.visit(this, successCounter.getStatistics(visitor.isResetStatistics()));
      visitor.visitErrorsBegin();
      visitor.visit(this, errorCounter.getStatistics(visitor.isResetStatistics()));
      visitor.visitErrorsEnd();
      visitor.visitEnd(this);
    }
  }
  
//  /**
//   * Return the statistics collected for all the events that succeeded.
//   */
//  public MetricStatistics getSuccessStatistics() {
//    return successStats;
//  }
//
//  /**
//   * Return the statistics collected for all the events that ended in error.
//   */
//  public MetricStatistics getErrorStatistics() {
//    return errorStats;
//  }

  /**
   * Updates the collected statistics.
   */
  public void updateStatistics2() {

//    List<TimedMetricEvent> successEvents = removeEvents(successQueue);
//    if (!successEvents.isEmpty()) {
//      successStats.update(successEvents);
//    }
//
//    List<TimedMetricEvent> errorEvents = removeEvents(errorQueue);
//    if (!errorEvents.isEmpty()) {
//      errorStats.update(errorEvents);
//    }
  }

//  private List<TimedMetricEvent> removeEvents(ConcurrentLinkedQueue<TimedMetricEvent> queue) {
//
//    ArrayList<TimedMetricEvent> events = new ArrayList<TimedMetricEvent>();
//    while (!queue.isEmpty()) {
//      TimedMetricEvent metricEvent = queue.remove();
//      events.add(metricEvent);
//    }
//    return events;
//  }

  public MetricName getName() {
    return name;
  }

  /**
   * Start an event.
   * <p>
   * The {@link TimedMetricEvent#endWithSuccess()} or
   * {@link TimedMetricEvent#endWithSuccess()} are called at the completion of
   * the timed event.
   * </p>
   */
  public TimedMetricEvent startEvent() {
    return new TimedMetricEvent(this);
  }

  /**
   * Called by {@link TimedMetricEvent#endWithSuccess()}.
   */
  protected void endWithSuccess(TimedMetricEvent event) {
    successCounter.add(event.getValue());
  }

  /**
   * Called by {@link TimedMetricEvent#endWithError()}.
   */
  protected void endWithError(TimedMetricEvent event) {
    errorCounter.add(event.getValue());
  }

}
