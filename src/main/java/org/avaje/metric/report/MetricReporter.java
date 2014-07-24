package org.avaje.metric.report;

import java.util.List;

import org.avaje.metric.Metric;

/**
 * Defines interface for reporting metrics.
 */
public interface MetricReporter {

  /**
   * Report the collected metrics. These metrics are all known to have non-empty values.
   */
  public void report(List<Metric> metrics);

  /**
   * Perform periodic cleanup of any resources (e.g. only keep x days of metrics files).
   * <p>
   * By default this will be called every 8 hours and is intended to be used to cleanup
   * old files created by the likes of FileReporter (only keep x days of metrics files).
   */
  public void cleanup();
}
