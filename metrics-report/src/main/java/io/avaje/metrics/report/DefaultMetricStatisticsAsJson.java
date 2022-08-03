package io.avaje.metrics.report;

import io.avaje.metrics.MetricManager;
import io.avaje.metrics.statistics.MetricStatisticsAsJson;

final class DefaultMetricStatisticsAsJson implements MetricStatisticsAsJson {

  @Override
  public void write(Appendable appendable) {
    JsonWriter.writeTo(appendable, MetricManager.collectMetrics());
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
