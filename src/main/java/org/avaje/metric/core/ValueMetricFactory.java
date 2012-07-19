package org.avaje.metric.core;

import java.util.concurrent.TimeUnit;

import org.avaje.metric.Clock;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;
import org.avaje.metric.ValueMetric;

public class ValueMetricFactory implements MetricFactory {

  @Override
  public Metric createMetric(MetricName name, TimeUnit rateUnit, Clock clock) {

    return new ValueMetric(name, rateUnit);
  }

}
