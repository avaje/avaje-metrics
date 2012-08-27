package org.avaje.metric.core;

import org.avaje.metric.Clock;
import org.avaje.metric.LoadMetric;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;

public class LoadMetricFactory implements MetricFactory {

  @Override
  public Metric createMetric(MetricName name, Clock clock) {

    return new LoadMetric(name);
  }

}
