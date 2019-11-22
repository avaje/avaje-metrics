package io.avaje.metrics.core;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.TimedMetric;

class TimedMetricFactory implements MetricFactory<TimedMetric> {

  @Override
  public TimedMetric createMetric(MetricName name, int[] bucketRanges) {
    return new DefaultTimedMetric(name);
  }

}
