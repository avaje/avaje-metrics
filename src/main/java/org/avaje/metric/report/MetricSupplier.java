package org.avaje.metric.report;

import org.avaje.metric.statistics.MetricStatistics;

import java.util.List;

/**
 * Supplier of additional metrics that should be included in reporting.
 */
public interface MetricSupplier {

  /**
   * Return extra metrics that should be included in metrics reporting.
   */
  List<MetricStatistics> collectMetrics();
}
