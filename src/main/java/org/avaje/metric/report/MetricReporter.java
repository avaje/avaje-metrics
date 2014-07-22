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
   * Perform periodic cleanup of any resources.
   */
  public void cleanup();
}
