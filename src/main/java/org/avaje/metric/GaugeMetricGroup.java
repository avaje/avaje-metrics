package org.avaje.metric;

import java.util.Arrays;

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
  public void visit(MetricVisitor visitor) {
    //boolean empty = stats.isEmpty();
    if (!visitor.visitBegin(this, false)) {
      // skip processing/reporting for empty metric
      // but nothing to reset on gauges
    } else {
      visitor.visit(this);
      visitor.visitEnd(this);
    }
  }

  @Override
  public void clearStatistics() {
    // nothing to do
  }
}
