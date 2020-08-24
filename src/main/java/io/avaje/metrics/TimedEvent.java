package io.avaje.metrics;

/**
 * A TimedEvent that is ended with either success or error.
 * <p>
 * Note that it is generally preferred to use {@link TimedMetric#addEventSince(boolean, long)} as
 * that avoids an object creation and the associated GC so has slightly less overhead.
 * <p>
 * Example:
 *
 * <pre>
 * <code>
 *  TimedMetric metric = MetricManager.timed(MyService.class, "sayHello");
 *  ...
 *
 *  TimedEvent timedEvent = metric.startEvent();
 *  try {
 *    ...
 *
 *  } finally {
 *    // Add the event to the 'success' statistics
 *    timedEvent.end();
 *  }
 *
 * </code>
 * </pre>
 *
 * @see TimedMetric#startEvent()
 */
public interface TimedEvent {

  /**
   * This timed event ended with successful execution.
   */
  void end();

  /**
   * This timed event ended with an error or fault execution.
   */
  void endWithError();

  /**
   * End specifying whether the event was successful or in error.
   */
  void end(boolean withSuccess);

}
