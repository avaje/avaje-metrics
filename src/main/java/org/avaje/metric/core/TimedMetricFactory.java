package org.avaje.metric.core;

import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;
import org.avaje.metric.TimedMetric;

public class TimedMetricFactory implements MetricFactory {

  @Override
  public Metric createMetric(MetricName name) {
    return new TimedMetric(name);
  }

}
