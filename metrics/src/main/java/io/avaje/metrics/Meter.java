package io.avaje.metrics;

/**
 * Metric that collects long values (e.g. total bytes sent).
 * <p>
 * Used when events have a value such as bytes sent, bytes received, lines read etc.
 * <pre>
 * <code>
 *  // Declare the metric (typically as a static field)
 *  static final Meter totalBytesSentMetric = Metrics.meter(MyService.class, "totalBytesSent");
 *  ...
 *
 *  public void performSomeIO() {
 *
 *    long bytesSent = ...
 *
 *    totalBytesSentMetric.addEvent(bytesSent);
 *    ...
 *  }
 *
 * </code>
 * </pre>
 */
public interface Meter extends Metric {

  /**
   * Add a value (bytes, time, rows etc).
   */
  void addEvent(long value);

  /**
   * Return the count of values collected (since the last reset/collection).
   */
  long count();

  /**
   * Return the total of all the values (since the last reset/collection).
   */
  long total();

  /**
   * Return the Max value collected (since the last reset/collection).
   */
  long max();

  /**
   * Return the mean value rounded up for the values collected since the last reset/collection.
   */
  long mean();

  /**
   * Statistics collected by Meter or Timer.
   */
  interface Stats extends Statistics {

    /**
     * Return the count of values collected (since the last reset/collection).
     */
    long count();

    /**
     * Return the total of all the values (since the last reset/collection).
     */
    long total();

    /**
     * Return the Max value collected (since the last reset/collection).
     */
    long max();

    /**
     * Return the mean value rounded up for the values collected since the last reset/collection.
     */
    long mean();

  }
}
