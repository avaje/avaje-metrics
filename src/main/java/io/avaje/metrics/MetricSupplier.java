package io.avaje.metrics;

import io.avaje.metrics.statistics.MetricStatistics;

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
