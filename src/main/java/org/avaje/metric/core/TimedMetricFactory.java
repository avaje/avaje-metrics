package org.avaje.metric.core;

import org.avaje.metric.Clock;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;
import org.avaje.metric.TimedMetric;

public class TimedMetricFactory implements MetricFactory {

  @Override
  public Metric createMetric(MetricName name, Clock clock) {

    return new TimedMetric(name, clock);
  }

}
