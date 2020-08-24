package io.avaje.metrics;

/**
 * Metric based on a counter (long value) typically used to count discrete events.
 * <p>
 * Can be used to count discrete events like 'user login'. {@link ValueMetric} would typically
 * be used when the event has a value (bytes sent, bytes received, lines read etc).
 * <pre>
 * <code>
 *  // Declare the counter (typically as a static field)
 *  static final CounterMetric userLoginCounter = MetricManager.counter(MyService.class, "userLogin");
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
public interface CounterMetric extends Metric {

  /**
   * Increment the counter by 1.
   */
  void inc();

  /**
   * Increment the counter by the given value.
   */
  void inc(long value);

  /**
   * Return the current count.
   */
  long getCount();

}
