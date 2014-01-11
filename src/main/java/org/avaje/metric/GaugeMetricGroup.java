package org.avaje.metric;

import java.util.Arrays;

import org.avaje.metric.report.MetricVisitor;

/**
 * A group of {@link GaugeMetric} that have a common base name.
 */
public class GaugeMetricGroup implements Metric {
  
  protected final MetricName baseName;
  
  protected final GaugeMetric[] metrics;
  
  /**
   * Create with a base name and list of metrics.
   */
  public GaugeMetricGroup(MetricName baseName, GaugeMetric... metrics) {
    this.baseName = baseName;
    this.metrics = metrics;
  }

  @Override
  public MetricName getName() {
    return baseName;
  }
  
  public String toString() {
    return Arrays.toString(metrics);
  }
  
  /**
   * Return the GaugeMetric's in this group.
   */
  public GaugeMetric[] getGaugeMetrics() {
    return metrics;
  }
  
  @Override
  public boolean collectStatistics() {
    // Considered to never be empty and nothing to reset
    return true;
  }

  @Override
  public void visitCollectedStatistics(MetricVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void clearStatistics() {
    // nothing to do
  }
}
