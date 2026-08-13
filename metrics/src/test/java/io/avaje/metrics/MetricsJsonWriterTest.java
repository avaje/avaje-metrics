package io.avaje.metrics;

import io.avaje.metrics.stats.CounterStats;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsJsonWriterTest {

  @Test
  void append_writesOriginalMetricJson() {
    var counter = new CounterStats(Metric.ID.of("app.counter", Tags.of("env:prod", "region:nz")), 3);
    var output = new StringBuilder();

    MetricsJsonWriter.append(output, java.util.List.of(counter));

    assertThat(output).contains("\"tags\":[\"env:prod\",\"region:nz\"]");
  }

  @Test
  void appendV2_writesCanonicalMetricJson() {
    var counter = new CounterStats(Metric.ID.of("app.counter", Tags.of("region:nz", "env:prod")), 3);
    var output = new StringBuilder();

    MetricsJsonWriter.appendV2(output, java.util.List.of(counter));

    assertThat(output).contains("\"tags\":\"env:prod,region:nz\"");
  }
}
