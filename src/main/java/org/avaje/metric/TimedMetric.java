package org.avaje.metric;

import java.util.concurrent.TimeUnit;

import org.avaje.metric.report.MetricVisitor;


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

  private ValueStatistics collectedSuccessStatistics;

  private ValueStatistics collectedErrorStatistics;
  
  public TimedMetric(MetricName name) {
    this.name = name;
  }

  public String toString() {
    return name.toString();
  }

  public ValueStatistics getCollectedSuccessStatistics() {
    return collectedSuccessStatistics;
  }

  public ValueStatistics getCollectedErrorStatistics() {
    return collectedErrorStatistics;
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
  
  @Override
  public boolean collectStatistics() {
    boolean empty = successCounter.isEmpty() && errorCounter.isEmpty();
    if (empty) {
      // just reset the start time
      successCounter.resetStartTime();
      errorCounter.resetStartTime();
    } else {
      // get a snapshot of the statistics and reset the underlying counters
      this.collectedSuccessStatistics = successCounter.getStatistics(true);
      this.collectedErrorStatistics = errorCounter.getStatistics(true);
    }
    return empty;
  }

  public void visitCollectedStatistics(MetricVisitor visitor) {
    visitor.visit(this);
  }
  
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
   * Add an event duration in nanoseconds noting if it was a success or failure result.
   * <p>
   * Success and failure statistics are kept separately.
   * </p>
   */
  public void addEventDuration(boolean success, long durationNanos) {
    if (success) {
      successCounter.add(TimeUnit.NANOSECONDS.toMicros(durationNanos));
    } else {
      errorCounter.add(TimeUnit.NANOSECONDS.toMicros(durationNanos));
    }
  }

}
