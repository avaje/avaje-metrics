package org.avaje.metric.report;

/**
 * A MetricReporter that does nothing.
 * <p>
 * Can be used as an alternative to turning off metrics reporting.
 * </p>
 */
public class NoopReporter implements MetricReporter {
  @Override
  public void report(ReportMetrics reportMetrics) {
    // do nothing
  }

  @Override
  public void cleanup() {
    // do nothing
  }
}
