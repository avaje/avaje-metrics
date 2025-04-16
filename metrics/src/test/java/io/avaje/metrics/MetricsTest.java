package io.avaje.metrics;

import io.avaje.metrics.stats.GaugeLongStats;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsTest {

  private final List<Metric.Statistics> suppliedMetrics = new ArrayList<>();

  @Test
  void addSupplier() {
    Metrics.collectMetrics();

    Metrics.addSupplier(() -> suppliedMetrics);
    suppliedMetrics.add(new GaugeLongStats(Metric.ID.of("supplied0"), 42));

    List<Metric.Statistics> result = Metrics.collectMetrics();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).name()).isEqualTo("supplied0");

    suppliedMetrics.clear();

    List<Metric.Statistics> result2 = Metrics.collectMetrics();
    assertThat(result2).isEmpty();
  }

}
