package org.avaje.metric.core;

import java.util.concurrent.TimeUnit;

import org.avaje.metric.Clock;
import org.avaje.metric.EventMetric;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;

public class EventMetricFactory implements MetricFactory {

  @Override
  public Metric createMetric(MetricName name, TimeUnit rateUnit, Clock clock) {

    return new EventMetric(name, rateUnit);
  }

}
