package io.avaje.metrics;

import java.util.List;

/**
 * API for requesting and
 */
public interface RequestTimingManager {

  /**
   * Return the request timings that have been collected since the last collection.
   */
  List<RequestTiming> collectRequestTimings();

  /**
   * Return all the timing metrics that are currently collecting per request timings and whose name
   * matches the name expression.
   * <p>
   * If the name match expression is null or empty then all timing metrics are returned.
   * </p>
   * <p>
   * These are TimingMetric that have {@link TimedMetric#getRequestTiming()}
   * greater than 0.
   * </p>
   * <h3>Example name match expressions:</h3>
   * <pre>{@code
   *
   *   // starts with web.
   *   "web.*"
   *
   *   // end with resource
   *   "*resource"
   *
   *   // starts with web. and contains customer
   *   "web.*customer*"
   *
   *   // starts with web. and contains customer and ends with resource
   *   "web.*customer*resource"
   *
   * }</pre>
   *
   * @param nameMatchExpression the expression used to match/filter metric names. Null or empty means match all.
   * @return timing metrics that are actively collecting request timings.
   */
  List<TimingMetricInfo> getRequestTimingMetrics(String nameMatchExpression);

  /**
   * Return the list of all timing metrics that match the name expression.
   * <p>
   * If the name match expression is null or empty then all timing metrics are returned.
   * </p>
   *
   * <h3>Example name match expressions:</h3>
   * <pre>{@code
   *
   *   // starts with web.
   *   "web.*"
   *
   *   // end with resource
   *   "*resource"
   *
   *   // starts with web. and contains customer
   *   "web.*customer*"
   *
   *   // starts with web. and contains customer and ends with resource
   *   "web.*customer*resource"
   *
   * }</pre>
   *
   * @param nameMatchExpression the expression used to match/filter metric names. Null or empty means match all.
   * @return all timing metrics those name matches the expression.
   */
  List<TimingMetricInfo> getAllTimingMetrics(String nameMatchExpression);

  /**
   * Set request timing on for a metric matching the name.
   *
   * @param collectionCount the number of requests to collect request timings for
   * @return true if request timing was set, false if the metric was not found.
   */
  boolean setRequestTimingCollection(String metricName, int collectionCount);

  /**
   * Set request timing on for a metric matching the class and name.
   *
   * @param collectionCount the number of requests to collect request timings for
   * @return true if request timing was set, false if the metric was not found.
   */
  boolean setRequestTimingCollection(Class<?> cls, String name, int collectionCount);

  /**
   * Set request timing on all the timed metrics whose name starts with a given prefix.
   * <p>
   * If for example all the web endpoints have a prefix of "web." then these can all be
   * set to collect say 10 requests.
   * </p>
   *
   * <h3>Example name match expressions:</h3>
   * <pre>{@code
   *
   *   // starts with web.
   *   "web.*"
   *
   *   // end with resource
   *   "*resource"
   *
   *   // starts with web. and contains customer
   *   "web.*customer*"
   *
   *   // starts with web. and contains customer and ends with resource
   *   "web.*customer*resource"
   *
   * }</pre>
   *
   * @param nameMatchExpression The expression used to match timing metrics
   * @param collectionCount     The number of requests to collect
   * @return The timing metrics that had the request timing collection set
   */
  List<TimingMetricInfo> setRequestTimingCollectionUsingMatch(String nameMatchExpression, int collectionCount);

}
