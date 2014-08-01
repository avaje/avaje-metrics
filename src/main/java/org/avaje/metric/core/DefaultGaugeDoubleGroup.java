package org.avaje.metric.core;

import java.util.Arrays;

import org.avaje.metric.GaugeDoubleGroup;
import org.avaje.metric.GaugeDoubleMetric;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;
import org.avaje.metric.MetricVisitor;

/**
 * A group of {@link DefaultGaugeDoubleMetric} that have a common base name.
 */
public class DefaultGaugeDoubleGroup implements Metric, GaugeDoubleGroup {
  
  protected final MetricName baseName;
  
  protected final GaugeDoubleMetric[] metrics;
  
  /**
   * Create with a base name and list of metrics.
   */
  public DefaultGaugeDoubleGroup(MetricName baseName, GaugeDoubleMetric... metrics) {
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
   * Return the GaugeDoubleMetric's in this group.
   */
  @Override
  public GaugeDoubleMetric[] getGaugeMetrics() {
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
