package io.avaje.metrics;

/**
 * Metric based on a counter (long value) typically used to count discrete events.
 * <p>
 * Can be used to count discrete events like 'user login'. {@link Meter} would typically
 * be used when the event has a value (bytes sent, bytes received, lines read etc).
 * <pre>
 * <code>
 *  // Declare the counter (typically as a static field)
 *  static final Counter userLoginCounter = Metrics.counter(MyService.class, "userLogin");
 *  ...
 *
 *  void performUserLogin() {
 *
 *    // increment the counter
 *    userLoginCounter.inc();
 *    ...
 *  }
 *
 * </code>
 * </pre>
 */
public interface Counter extends Metric {

  /**
   * Increment the counter by 1.
   */
  void inc();

  /**
   * Increment the counter by the given value.
   */
  void inc(long value);

  /**
   * Decrement the counter by 1.
   */
  void dec();

  /**
   * Decrement the counter by the given value.
   */
  void dec(long value);

  /**
   * Return the current count.
   */
  long count();

  /**
   * Statistics provided by the {@link Counter}.
   */
  interface Stats extends MetricStats {

    /**
     * Return the count of values collected.
     */
    long count();
  }
}
