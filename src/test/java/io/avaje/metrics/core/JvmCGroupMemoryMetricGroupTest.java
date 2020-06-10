package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLongMetric;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertTrue;

public class JvmCGroupMemoryMetricGroupTest {

  private final JvmCGroupMemoryMetricGroup me = new JvmCGroupMemoryMetricGroup();

  @Test
  public void toMegaBytes() {

    assertThat(JvmCGroupMemoryMetricGroup.toMegaBytes(1074790400)).isEqualTo(1025);
    assertThat(JvmCGroupMemoryMetricGroup.toMegaBytes(238493696)).isEqualTo(227);
  }

  @Test
  public void memSource() {

    long limit = 1074790400;

    FileLines useSource = new FileLines("src/test/resources/cgroup/memory.usage_in_bytes");
    assertTrue(useSource.exists());

    JvmCGroupMemoryMetricGroup.MemSource source = new JvmCGroupMemoryMetricGroup.MemSource(limit, useSource);

    assertThat(source.getLimitMb()).isEqualTo(1025);
    assertThat(source.getUsageMb()).isEqualTo(227);
    assertThat(source.getPctUsage()).isEqualTo(22);

    final GaugeLongMetric memoryLimit = me.createMemoryLimit(source, true);
    assertThat(memoryLimit.getValue()).isEqualTo(1025);

    final GaugeLongMetric memoryUsage = me.createMemoryUsage(source, true);
    assertThat(memoryUsage.getValue()).isEqualTo(227);

    final GaugeLongMetric pctUsage = me.createMemoryPctUsage(source, true);
    assertThat(pctUsage.getValue()).isEqualTo(22);
  }

}