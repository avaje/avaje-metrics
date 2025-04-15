package io.avaje.metrics;

import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A Timer for measuring execution time for methods and events.
 * <p>
 * Timers have microsecond precision.
 *
 * <h3>Adding Timer via <code>@Timed</code> and enhancement</h3>
 * <p>
 * We can use timed metric by putting <code>@Timed</code> annotation on
 * a class. When we do that all public methods of that class will have timing
 * added via enhancement.
 * <p>
 * We can put <code>@Timed</code> on private methods that we want to have timed
 * (as private methods don't have timing added by default).
 * <p>
 * We can put <code>@NotTimed</code> on public methods that we don't want timing on.
 *
 * <h3>Adding Timer via code</h3>
 * <p>
 * Alternatively we can use Timer via writing code ourselves to get the
 * Timer and use timer event like the following example:
 *
 * <pre>
 * <code>
 *  Timer timer = Metrics.timer(MyService.class, "sayHello");
 *  ...
 *
 *  Timer.Event event = timer.startEvent();
 *  try {
 *    ...
 *
 *  } finally {
 *    // Add the event to the 'success' statistics
 *    event.end();
 *  }
 *
 * </code>
 * </pre>
 *
 * <p>
 * Alternatively we can add event timing using a start time in nanos
 * like the following example:
 *
 * <pre>
 * <code>
 *  Timer timer = Metrics.timer(MyService.class, "sayHello");
 *  ...
 *
 *  long startNanos = System.nanoTime();
 *  try {
 *    ...
 *
 *  } finally {
 *    timer.add(startNanos);
 *  }
 *
 * </code>
 * </pre>
 */
public interface Timer extends Metric {

  /**
   * Times the execution of the event.
   */
  void time(Runnable event);

  /**
   * Times the execution of the event.
   */
  <T> T time(Supplier<T> event);

  /**
   * Start an event.
   * <p>
   * At the completion of the event one of {@link Event#end()},
   * {@link Event#endWithError()} or {@link Event#end(boolean)} is called to record the
   * event duration and success or otherwise.
   * <p>
   * This is an alternative to using {@link #addEventSince(boolean, long)} or
   * {@link #addEventDuration(boolean, long)}. Note that this startEvent() method has slightly
   * higher overhead as it instantiates a TimedEvent object which must be later GC'ed. In this sense
   * generally addEventSince() is the preferred method to use.
   */
  Event startEvent();

  /**
   * Add an event based on a startNanos (determined by {@link System#nanoTime()}).
   * <p>
   * Success and failure statistics are kept separately.
   * <p>
   * This is an alternative to using {@link #startEvent()}. Note that using startEvent() has
   * slightly higher overhead as it instantiates a TimedEvent object which must be later GC'ed. In
   * this sense generally addEventSince() is the preferred method to use.
   */
  void addEventSince(boolean success, long startNanos);

  /**
   * Add a successful event duration.
   */
  void add(long startNanos);

  /**
   * Add an error event duration to the error.
   * <p>
   * Success and error execution are kept on separate metrics.
   */
  void addErr(long startNanos);

  /**
   * Add an event duration in nanoseconds noting if it was a success or failure result.
   * <p>
   * Success and failure statistics are kept separately.
   * <p>
   * This is an alternative to using {@link #addEventSince(boolean, long)} where you pass in the
   * duration rather than the start nanoseconds.
   */
  void addEventDuration(boolean success, long durationNanos);

  /**
   * Return the bucket range or empty string if not a bucket.
   */
  @Nullable String bucketRange();

  /**
   * Statistics collected by Timer.
   */
  interface Stats extends Meter.Stats {

    /**
     * Return the bucket range for these statistics.
     */
    default @Nullable String bucketRange() {
      return null;
    }
  }

  /**
   * A TimedEvent that is ended with either success or error.
   * <p>
   * Note that it is generally preferred to use {@link Timer#addEventSince(boolean, long)} as
   * that avoids an object creation and the associated GC so has slightly less overhead.
   * <p>
   * Example:
   *
   * <pre>
   * <code>
   *  Timer timer = Metrics.timer(MyService.class, "sayHello");
   *  ...
   *
   *  Timer.Event event = timer.startEvent();
   *  try {
   *    ...
   *
   *  } finally {
   *    // Add the event to the 'success' statistics
   *    event.end();
   *  }
   *
   * </code>
   * </pre>
   *
   * @see Timer#startEvent()
   */
  interface Event {

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
}
