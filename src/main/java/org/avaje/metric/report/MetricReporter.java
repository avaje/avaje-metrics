package org.avaje.metric.report;

/**
 * Defines interface for reporting metrics.
 * <p>
 * Construct and then pass the MetricReporter into one of the constructors for MetricReportManager.
 */
public interface MetricReporter {

  /**
   * Report the collected metrics. These metrics are all known to have non-empty values.
   */
  void report(ReportMetrics reportMetrics);

  /**
   * Perform periodic cleanup of any resources (e.g. only keep x days of metrics files).
   * <p>
   * By default this will be called approximately every 8 hours and is intended to be used to cleanup old files
   * created by the likes of FileReporter (only keep x days of metrics files).
   */
  void cleanup();
}
