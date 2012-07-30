package org.avaje.metric;

/**
 * Defines a filter or predicate used to match metrics.
 */
public interface MetricMatcher {

  /**
   * Return true if the metric is a match.
   */
  public boolean isMatch(Metric metric);
  
}
