package io.avaje.metrics.core;

import io.avaje.metrics.report.JsonWriter;
import io.avaje.metrics.statistics.MetricStatisticsAsJson;

final class DefaultMetricStatisticsAsJson implements MetricStatisticsAsJson {

  private final DefaultMetricManager manager;

  DefaultMetricStatisticsAsJson(DefaultMetricManager manager) {
    this.manager = manager;
  }

  @Override
  public void write(Appendable appendable) {
    JsonWriter.writeTo(appendable, manager.collectMetrics());
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
