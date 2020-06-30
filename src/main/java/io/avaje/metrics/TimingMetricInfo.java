package io.avaje.metrics;

/**
 * Bean holding timing metric name and collection count.
 * <p>
 * This is simple data bean intended to be passed to a front end, converted to JSON etc.
 * </p>
 */
public class TimingMetricInfo {

  final String name;

  final int collectionCount;

  /**
   * Construct with metric name and collection count.
   */
  public TimingMetricInfo(String name, int collectionCount) {
    this.name = name;
    this.collectionCount = collectionCount;
  }

  /**
   * Return the metric name.
   */
  public String getName() {
    return name;
  }

  /**
   * Return the current collection count for this metric.
   * <p>
   * This is the number of remaining requests that will have timing collected.
   * </p>
   */
  public int getCollectionCount() {
    return collectionCount;
  }
}
