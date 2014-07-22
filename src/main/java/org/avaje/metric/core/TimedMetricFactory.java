package org.avaje.metric.core;

import org.avaje.metric.MetricName;
import org.avaje.metric.TimedMetric;

public class TimedMetricFactory implements MetricFactory<TimedMetric> {

  @Override
  public TimedMetric createMetric(MetricName name) {
    return new DefaultTimedMetric(name);
  }

}
