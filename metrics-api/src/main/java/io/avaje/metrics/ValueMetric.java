package io.avaje.metrics;

/**
 * Metric that collects long values (e.g. total bytes sent).
 * <p>
 * Used when events have a value such as bytes sent, bytes received, lines read etc.
 * <pre>
 * <code>
 *  // Declare the metric (typically as a static field)
 *  static final ValueMetric totalBytesSentMetric = MetricManager.value(MyService.class, "totalBytesSent");
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
public interface ValueMetric extends Metric {

  /**
   * Add a value (bytes, time, rows etc).
   */
  void addEvent(long value);

  /**
   * Return the count of values collected (since the last reset/collection).
   */
  long getCount();

  /**
   * Return the total of all the values (since the last reset/collection).
   */
  long getTotal();

  /**
   * Return the Max value collected (since the last reset/collection).
   */
  long getMax();

  /**
   * Return the mean value rounded up for the values collected since the last reset/collection.
   */
  long getMean();

}
