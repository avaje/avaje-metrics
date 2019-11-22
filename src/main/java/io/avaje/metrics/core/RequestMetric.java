package io.avaje.metrics.core;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.core.noop.NoopTimedMetric;

import java.util.Map;

class RequestMetric extends NoopTimedMetric {

  private final Map<String, String> attributes;

  RequestMetric(MetricName name) {
    super(name);
    this.attributes = null;
  }

  RequestMetric(MetricName name, Map<String, String> attributes) {
    super(name);
    this.attributes = attributes;
  }

  @Override
  public Map<String, String> attributes() {
    return attributes;
  }
}
