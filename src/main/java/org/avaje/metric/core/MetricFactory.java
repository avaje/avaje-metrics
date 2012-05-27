package org.avaje.metric.core;

import java.util.concurrent.TimeUnit;

import org.avaje.metric.Clock;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;

public interface MetricFactory {

  public abstract Metric createMetric(MetricName name, TimeUnit rateUnit, Clock clock, JmxMetricRegister jmxRegistry);

}