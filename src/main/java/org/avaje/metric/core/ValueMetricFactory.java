package org.avaje.metric.core;

import java.util.concurrent.TimeUnit;

import org.avaje.metric.Clock;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;
import org.avaje.metric.ValueMetric;

public class ValueMetricFactory implements MetricFactory {

  @Override
  public Metric createMetric(MetricName name, TimeUnit rateUnit, Clock clock, JmxMetricRegister jmxRegistry) {

    ValueMetric m = new ValueMetric(name, rateUnit);
        
    //MetricMXBean mxBean = jmxRegistry.createMetricMXBean(m.getStatistics());
    //jmxRegistry.register(mxBean, m.getName().getMBeanObjectName());
    
    return m;
  }


  
}
