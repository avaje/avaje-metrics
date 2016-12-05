package org.avaje.metric.core;

import org.avaje.metric.MetricName;
import org.avaje.metric.core.noop.NoopTimedMetric;

import java.util.Map;

public class RequestMetric extends NoopTimedMetric {

  private final Map<String, String> attributes;

  public RequestMetric(MetricName name) {
    super(name);
    this.attributes = null;
  }

  public RequestMetric(MetricName name, Map<String, String> attributes) {
    super(name);
    this.attributes = attributes;
  }

  @Override
  public Map<String, String> attributes() {
    return attributes;
  }
}
