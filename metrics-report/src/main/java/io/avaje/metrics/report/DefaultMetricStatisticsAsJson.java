package io.avaje.metrics.report;

import io.avaje.metrics.Metrics;

final class DefaultMetricStatisticsAsJson implements MetricStatisticsAsJson {

  @Override
  public void write(Appendable appendable) {
    JsonWriter.writeTo(appendable, Metrics.collectMetrics());
  }

  @Override
  public String asJson() {
    StringBuilder buffer = new StringBuilder(1000);
    buffer.append("[");
    write(buffer);
    buffer.append("]");
    return buffer.toString();
  }
}
