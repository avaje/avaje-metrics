package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JvmCGroupMemoryMetricGroupTest {

  private final JvmCGroupMemoryMetricGroup me = new JvmCGroupMemoryMetricGroup();

  @Test
  void toMegaBytes() {
    assertThat(JvmCGroupMemoryMetricGroup.toMegaBytes(1074790400)).isEqualTo(1025);
    assertThat(JvmCGroupMemoryMetricGroup.toMegaBytes(238493696)).isEqualTo(227);
  }

  @Test
  void memSource() {

    long limit = 1074790400;

    FileLines useSource = new FileLines("src/test/resources/cgroup/memory.usage_in_bytes");
    assertTrue(useSource.exists());

    JvmCGroupMemoryMetricGroup.MemSource source = new JvmCGroupMemoryMetricGroup.MemSource(limit, useSource);

    assertThat(source.getLimitMb()).isEqualTo(1025);
    assertThat(source.getUsageMb()).isEqualTo(227);
    assertThat(source.getPctUsage()).isEqualTo(22);

    final GaugeLong memoryLimit = me.createMemoryLimit(source, true);
    assertThat(memoryLimit.value()).isEqualTo(1025);

    final GaugeLong memoryUsage = me.createMemoryUsage(source, true);
    assertThat(memoryUsage.value()).isEqualTo(227);

    final GaugeLong pctUsage = me.createMemoryPctUsage(source, true);
    assertThat(pctUsage.value()).isEqualTo(22);
  }

}
