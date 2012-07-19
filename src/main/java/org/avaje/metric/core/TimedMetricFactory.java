package org.avaje.metric.core;

import java.util.concurrent.TimeUnit;

import org.avaje.metric.Clock;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;
import org.avaje.metric.TimedMetric;

public class TimedMetricFactory implements MetricFactory {


  @Override
  public Metric createMetric(MetricName name, TimeUnit rateUnit, Clock clock, JmxMetricRegister jmxRegistry) {
    
    TimedMetric timedMetric = new TimedMetric(name, rateUnit, clock);
//    MetricName metricName = timedMetric.getName();
//    jmxRegistry.register(createMxBean(timedMetric.getSuccessStatistics(), jmxRegistry), metricName.getMBeanObjectName());
//    jmxRegistry.register(createMxBean(timedMetric.getErrorStatistics(), jmxRegistry), timedMetric.getErrorMBeanName());
    
    return timedMetric;
  }
  
//  private MetricMXBean createMxBean(MetricStatistics stats, JmxMetricRegister jmxRegistry) {
//    return jmxRegistry.createMetricMXBean(stats);
//  }
  
}
