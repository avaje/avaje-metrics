package io.avaje.metrics;

import java.util.function.Supplier;

/**
 * A TimedMetric for measuring execution time for methods and events.
 *
 * <h3>Adding TimedMetric via <code>@Timed</code> and enhancement</h3>
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
 * <h3>Adding TimedMetric via code</h3>
 * <p>
 * Alternatively we can use TimedMetric via writing code ourselves to get the
 * TimedMetric and use TimedEvent like the following example:
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
 * <p>
 * Alternatively we can add event timing using a start time in nanos
 * like the following example:
 *
 * <pre>
 * <code>
 *  TimedMetric metric = MetricManager.timed(MyService.class, "sayHello");
 *  ...
 *
 *  long startNanos = System.nanoTime();
 *  try {
 *    ...
 *
 *  } finally {
 *    metric.add(startNanos);
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
   * Add a successful event duration with request timing.
   */
  void add(long startNanos, boolean requestTiming);

  /**
   * Add an error event duration to the error.
   * <p>
   * Success and error execution are kept on separate metrics.
   */
  void addErr(long startNanos);

  /**
   * Add an error event duration with request timing.
   * <p>
   * Success and error execution are kept on separate metrics.
   */
  void addErr(long startNanos, boolean requestTiming);

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
   * Return true if this timed metric is part of a bucket range (and hence only hold statistics for the
   * bucket range returned by <code>bucketRange()</code>.
   */
  boolean isBucket();

  /**
   * Return the bucket range or empty string if not a bucket.
   */
  String bucketRange();

//  /**
//   * Return true if this timed metric is actively request timing.
//   * <p>
//   * This means that the current thread is actively collecting timing entries and this metric
//   * has been pushed onto the nested context.
//   */
//  boolean isRequestTiming();
//
//  /**
//   * Specify to collect per request detailed timing collection. The collectionCount is the number
//   * of requests to collect detailed timing for and then automatically turn off.
//   * <p>
//   * This is expected to only be explicitly called on 'top level' metrics such as web endpoints.
//   * Once a request timing context has been created by the top level metric then 'nested metrics'
//   * (typically service and data access layers) can append to that existing context.  In this way
//   * detailed per request level timing entries can be collected for only selected endpoints.
//   */
//  void setRequestTiming(int collectionCount);
//
//  /**
//   * Return the number of remaining requests to collect detailed timing on.
//   * <p>
//   * This value starts out as the value set by #setRequestTimingCollection and then decrements
//   * after a request timing is reported until it reaches 0 at which point request timing automatically
//   * turns off for this metric.
//   */
//  int getRequestTiming();
//
//  /**
//   * Decrement the request timing collection count.
//   * <p>
//   * This is typically called internally when a request timing is reported and generally not
//   * expected to be called by application code.
//   */
//  void decrementRequestTiming();
//
//  /**
//   * Return extra attributes that can be included in the request logging.
//   */
//  Map<String, String> attributes();

  /**
   * Statistics collected by TimedMetric.
   */
  interface Stats extends Meter.Stats {

    /**
     * Return true if this is bucket range based.
     */
    boolean isBucket();

    /**
     * Return the bucket range for these statistics.
     */
    String bucketRange();

    /**
     * Return the metric name with bucket tag if necessary.
     * <p>
     * If the timed metric is a bucket it gets a "tag" appended
     * like <code>";bucket=.."</code>
     * </p>
     */
    String nameWithBucket();
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
