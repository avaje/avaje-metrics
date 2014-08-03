package org.avaje.metric.core;

import java.util.concurrent.TimeUnit;

import org.avaje.metric.MetricName;
import org.avaje.metric.MetricVisitor;
import org.avaje.metric.TimedEvent;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.ValueStatistics;

/**
 * Designed to capture the duration of timed events.
 * <p>
 * The major difference compared with ValueMetric is that it is specifically oriented towards
 * collecting time duration and provides separate statistics for success and error completion.
 * </p>
 */
public final class DefaultTimedMetric implements TimedMetric {

  private final MetricName name;

  private final ValueCounter successCounter = new ValueCounter();

  private final ValueCounter errorCounter = new ValueCounter();

  private ValueStatistics collectedSuccessStatistics;

  private ValueStatistics collectedErrorStatistics;

  public DefaultTimedMetric(MetricName name) {
    this.name = name;
  }

  public String toString() {
    return name.toString();
  }

  @Override
  public ValueStatistics getCollectedSuccessStatistics() {
    return collectedSuccessStatistics;
  }

  @Override
  public ValueStatistics getCollectedErrorStatistics() {
    return collectedErrorStatistics;
  }

  @Override
  public ValueStatistics getSuccessStatistics(boolean reset) {
    return successCounter.getStatistics(reset);
  }

  @Override
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
    return !empty;
  }

  @Override
  public void visit(MetricVisitor visitor) {
    visitor.visit(this);
  }

  public MetricName getName() {
    return name;
  }

  /**
   * Start an event.
   * <p>
   * The {@link TimedEvent#endWithSuccess()} or {@link TimedEvent#endWithError()} are called at the
   * completion of the timed event.
   * </p>
   */
  @Override
  public TimedEvent startEvent() {
    return new DefaultTimedMetricEvent(this);
  }

  /**
   * Add an event duration in nanoseconds noting if it was a success or failure result.
   * <p>
   * Success and failure statistics are kept separately.
   * </p>
   */
  @Override
  public void addEventDuration(boolean success, long durationNanos) {
    if (success) {
      successCounter.add(TimeUnit.NANOSECONDS.toMicros(durationNanos));
    } else {
      errorCounter.add(TimeUnit.NANOSECONDS.toMicros(durationNanos));
    }
  }

  
  /**
   * Add an event with duration calculated based on startNanos. 
   */
  @Override
  public void addEventSince(boolean success, long startNanos) {
    addEventDuration(success, System.nanoTime() - startNanos);
  }

  /**
   * Add an event duration with opCode indicating success or failure. This is intended for use by
   * enhanced code and not general use.
   */
  @Override
  public void operationEnd(int opCode, long startNanos) {
    // OpCodes.ATHROW = 191
    addEventSince(opCode != 191, startNanos);
  }

}
