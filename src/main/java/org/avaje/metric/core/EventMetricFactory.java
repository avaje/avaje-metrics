package org.avaje.metric.core;

import java.util.concurrent.TimeUnit;

import org.avaje.metric.Clock;
import org.avaje.metric.EventMetric;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;

public class EventMetricFactory implements MetricFactory {

  @Override
  public Metric createMetric(MetricName name, TimeUnit rateUnit, Clock clock, JmxMetricRegister jmxRegistry) {

    EventMetric m = new EventMetric(name, rateUnit);
        
    RateStatisticsMXBean mxBean = jmxRegistry.createMetricMXBean(m.getStatistics());
    jmxRegistry.register(mxBean, m.getName().getMBeanObjectName());
    
    return m;
  }


  
}
