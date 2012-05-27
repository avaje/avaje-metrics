package org.avaje.metric;

import java.util.concurrent.TimeUnit;

class FactoryHelper {

  public EventMetric newEventMetric(MetricName name, TimeUnit rateUnit) {
    return new EventMetric(name, rateUnit);
  }
}
