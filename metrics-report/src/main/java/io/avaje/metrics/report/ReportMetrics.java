package io.avaje.metrics.report;

import io.avaje.metrics.statistics.MetricStatistics;

import java.util.List;

/**
 * Wrapper that holds the various information that can be reported.
 */
public class ReportMetrics {

  /**
   * This is the environment specific information - Application, Environment, Server etc.
   */
  protected final HeaderInfo headerInfo;

  /**
   * The time the metrics were collected. This is used to determine the duration for each metric
   * which is the time since its last collection.
   */
  protected final long collectionTime;

  /**
   * The metrics that were collected that were not empty. Metrics that didn't have events occur are
   * considered empty and not reported.
   */
  protected final List<MetricStatistics> metrics;

  /**
   * The reporting frequency in seconds.
   */
  protected final int freqInSeconds;

  /**
   * Construct with the details.
   *
   * @param headerInfo     This is the environment specific information - Application, Environment, Server etc.
   * @param collectionTime The time the metrics were collected. This is used to determine the duration for each
   *                       metric which is the time since its last collection.
   * @param metrics        The metrics that were collected that were not empty. Metrics that didn't have events
   * @param freqInSeconds  The reporting frequency in seconds.
   */
  public ReportMetrics(HeaderInfo headerInfo, long collectionTime, List<MetricStatistics> metrics, int freqInSeconds) {
    this.headerInfo = headerInfo;
    this.collectionTime = collectionTime;
    this.metrics = metrics;
    this.freqInSeconds = freqInSeconds;
  }

  /**
   * Return the HeaderInfo.
   */
  public HeaderInfo getHeaderInfo() {
    return headerInfo;
  }

  /**
   * Return the time the metrics were collected.
   */
  public long getCollectionTime() {
    return collectionTime;
  }

  /**
   * Return the metrics that were collected.
   */
  public List<MetricStatistics> getMetrics() {
    return metrics;
  }

  /**
   * Return the reporting frequency in seconds.
   */
  public int getFreqInSeconds() {
    return freqInSeconds;
  }
}
