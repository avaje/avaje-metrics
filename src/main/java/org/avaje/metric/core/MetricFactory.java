package org.avaje.metric.core;

import org.avaje.metric.Clock;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;

/**
 * Factory for creating metrics.
 */
public interface MetricFactory {

  /**
   * Create the metric.
   */
  public Metric createMetric(MetricName name, Clock clock);

}