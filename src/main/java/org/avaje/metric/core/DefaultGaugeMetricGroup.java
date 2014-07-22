package org.avaje.metric.core;

import java.util.Arrays;

import org.avaje.metric.GaugeMetric;
import org.avaje.metric.GaugeMetricGroup;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;
import org.avaje.metric.MetricVisitor;

/**
 * A group of {@link DefaultGaugeMetric} that have a common base name.
 */
public class DefaultGaugeMetricGroup implements Metric, GaugeMetricGroup {
  
  protected final MetricName baseName;
  
  protected final GaugeMetric[] metrics;
  
  /**
   * Create with a base name and list of metrics.
   */
  public DefaultGaugeMetricGroup(MetricName baseName, GaugeMetric... metrics) {
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
  @Override
  public GaugeMetric[] getGaugeMetrics() {
    return metrics;
  }
  
  @Override
  public boolean collectStatistics() {
    // Considered to never be empty and nothing to reset
    return true;
  }

  @Override
  public void visit(MetricVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void clearStatistics() {
    // nothing to do
  }
}
