package org.avaje.metric.core;

import java.util.concurrent.TimeUnit;

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
  public abstract Metric createMetric(MetricName name, TimeUnit rateUnit, Clock clock);

}