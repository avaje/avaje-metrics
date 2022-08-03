package io.avaje.metrics.report;

import io.avaje.metrics.statistics.MetricStatistics;

import java.util.List;

/**
 * Aggregates metrics typically with a common prefix like "web.api" or "db.query".
 */
public interface MetricReportAggregator {

  /**
   * Process the metric statistics.
   */
  void process(List<MetricStatistics> statistics);
}
