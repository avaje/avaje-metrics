package org.avaje.metric.core;

import java.io.IOException;
import java.util.Arrays;

import org.avaje.metric.GaugeLongMetric;
import org.avaje.metric.GaugeLongGroup;
import org.avaje.metric.MetricName;
import org.avaje.metric.MetricVisitor;

/**
 * A group of {@link DefaultGaugeDoubleMetric} that have a common base name.
 */
public class DefaultGaugeLongGroup implements GaugeLongGroup {
  
  protected final MetricName baseName;
  
  protected final GaugeLongMetric[] metrics;
  
  /**
   * Create with a base name and list of metrics.
   */
  public DefaultGaugeLongGroup(MetricName baseName, GaugeLongMetric... metrics) {
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
  public GaugeLongMetric[] getGaugeMetrics() {
    return metrics;
  }
  
  @Override
  public boolean collectStatistics() {
    // Considered to never be empty and nothing to reset
    return true;
  }

  @Override
  public void visit(MetricVisitor visitor) throws IOException {
    visitor.visit(this);
  }

  @Override
  public void clearStatistics() {
    // nothing to do
  }
}
