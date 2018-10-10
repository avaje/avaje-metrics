package org.avaje.metric.core;

import org.avaje.metric.MetricName;
import org.avaje.metric.TimedMetric;

class TimedMetricFactory implements MetricFactory<TimedMetric> {

  @Override
  public TimedMetric createMetric(MetricName name, int[] bucketRanges) {
    return new DefaultTimedMetric(name);
  }

}
